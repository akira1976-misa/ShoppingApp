package com.shopping.pricecompare.api

import com.google.gson.Gson
import com.shopping.pricecompare.BuildConfig
import com.shopping.pricecompare.api.model.NaverShoppingResponse
import com.shopping.pricecompare.util.RateLimiter
import com.shopping.pricecompare.util.RetryPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * /skill-crawler: 네이버쇼핑 API 연동.
 * - Timeout 설정 (무한 로딩 방지)
 * - DEBUG 빌드에서만 로그 출력 (릴리즈 API 키 노출 방지)
 * - Rate Limiter: 초당 5회 제한
 * - Retry: 429/5xx만 재시도, 인증 오류는 즉시 실패
 */
object NaverShoppingService {
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)   // 연결 대기 최대 10초
        .readTimeout(15, TimeUnit.SECONDS)       // 응답 읽기 최대 15초
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            // DEBUG 빌드에서만 로그 출력 (API 키가 헤더에 노출되므로 릴리즈에선 비활성)
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val rateLimiter = RateLimiter(maxCalls = 5, windowMs = 1_000L)

    data class PagedResult(
        val items: List<com.shopping.pricecompare.api.model.NaverShoppingItem>,
        val totalCount: Int, val currentPage: Int, val pageSize: Int
    ) {
        val totalPages: Int get() = if (totalCount <= 0) 1 else minOf(8, (totalCount + pageSize - 1) / pageSize)
        val hasNext: Boolean get() = currentPage < totalPages
        val hasPrev: Boolean get() = currentPage > 1
    }

    suspend fun searchPaged(
        query: String, page: Int = 1, pageSize: Int = 120, sort: String = "sim"
    ): PagedResult = withContext(Dispatchers.IO) {
        val clientId = BuildConfig.NAVER_CLIENT_ID
        val clientSecret = BuildConfig.NAVER_CLIENT_SECRET
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            return@withContext PagedResult(emptyList(), 0, page, pageSize)
        }

        val startIndex = (page - 1) * pageSize + 1
        val items = mutableListOf<com.shopping.pricecompare.api.model.NaverShoppingItem>()

        val r1 = fetchWithRetry(query, minOf(pageSize, 100), startIndex, sort, clientId, clientSecret)
        items.addAll(r1.items)

        val remaining = pageSize - items.size
        if (remaining > 0 && startIndex + 100 <= 1000) {
            val r2 = fetchWithRetry(query, remaining, startIndex + 100, sort, clientId, clientSecret)
            items.addAll(r2.items)
        }

        PagedResult(items, r1.total, page, pageSize)
    }

    private suspend fun fetchWithRetry(
        query: String, display: Int, start: Int, sort: String,
        clientId: String, clientSecret: String
    ): NaverShoppingResponse {
        return try {
            RetryPolicy.withRetry(
                maxAttempts = 3,
                initialDelayMs = 300L,
                shouldRetry = { e -> e is RetryableApiException }
            ) {
                rateLimiter.acquire()
                fetchRaw(query, display, start, sort, clientId, clientSecret)
            }
        } catch (e: Exception) {
            NaverShoppingResponse()
        }
    }

    private class RetryableApiException(message: String) : IOException(message)

    private suspend fun fetchRaw(
        query: String, display: Int, start: Int, sort: String,
        clientId: String, clientSecret: String
    ): NaverShoppingResponse = withContext(Dispatchers.IO) {
        val q = URLEncoder.encode(query, "UTF-8")
        val url = "https://openapi.naver.com/v1/search/shop.json?query=$q&display=$display&start=$start&sort=$sort"
        val req = Request.Builder().url(url)
            .addHeader("X-Naver-Client-Id", clientId)
            .addHeader("X-Naver-Client-Secret", clientSecret)
            .get().build()

        val res = client.newCall(req).execute()
        when {
            res.isSuccessful ->
                gson.fromJson(res.body?.string() ?: "", NaverShoppingResponse::class.java) ?: NaverShoppingResponse()
            res.code == 429 ->
                throw RetryableApiException("네이버 API 429 Too Many Requests")
            res.code in 500..599 ->
                throw RetryableApiException("네이버 API 서버 오류 ${res.code}")
            else -> NaverShoppingResponse()
        }
    }
}
