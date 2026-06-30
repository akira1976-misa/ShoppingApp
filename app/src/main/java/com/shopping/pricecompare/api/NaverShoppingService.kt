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

/**
 * /skill-crawler: 네이버쇼핑 API 연동.
 * - 일일 25,000건 제한 → RateLimiter로 초당 호출 수 제어
 * - 일시적 오류(429/5xx/네트워크) → RetryPolicy로 지수 백오프 재시도
 * - 영구적 오류(401/403 등 인증 실패) → 즉시 실패 처리(재시도 안 함)
 */
object NaverShoppingService {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    // 초당 5회로 제한 (네이버 일일 한도 내에서 안전하게 운용)
    private val rateLimiter = RateLimiter(maxCalls = 5, windowMs = 1_000L)

    data class PagedResult(
        val items: List<com.shopping.pricecompare.api.model.NaverShoppingItem>,
        val totalCount: Int, val currentPage: Int, val pageSize: Int
    ) {
        val totalPages: Int get() = if (totalCount <= 0) 1 else minOf(8, (totalCount + pageSize - 1) / pageSize)
        val hasNext: Boolean get() = currentPage < totalPages
        val hasPrev: Boolean get() = currentPage > 1
    }

    suspend fun searchPaged(query: String, page: Int = 1, pageSize: Int = 120, sort: String = "sim"): PagedResult =
        withContext(Dispatchers.IO) {
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

    /** Rate Limiter + 재시도가 적용된 호출 래퍼 */
    private suspend fun fetchWithRetry(
        query: String, display: Int, start: Int, sort: String, clientId: String, clientSecret: String
    ): NaverShoppingResponse {
        return try {
            RetryPolicy.withRetry(
                maxAttempts = 3,
                initialDelayMs = 300L,
                shouldRetry = { e -> e is RetryableApiException } // 일시적 오류만 재시도
            ) {
                rateLimiter.acquire() // 호출 직전 속도 제한 체크
                fetchRaw(query, display, start, sort, clientId, clientSecret)
            }
        } catch (e: Exception) {
            // 모든 재시도가 실패하면 빈 결과 반환 (앱이 죽지 않도록)
            NaverShoppingResponse()
        }
    }

    private class RetryableApiException(message: String) : IOException(message)

    private suspend fun fetchRaw(
        query: String, display: Int, start: Int, sort: String, clientId: String, clientSecret: String
    ): NaverShoppingResponse = withContext(Dispatchers.IO) {
        val q = URLEncoder.encode(query, "UTF-8")
        val url = "https://openapi.naver.com/v1/search/shop.json?query=$q&display=$display&start=$start&sort=$sort"
        val req = Request.Builder().url(url)
            .addHeader("X-Naver-Client-Id", clientId)
            .addHeader("X-Naver-Client-Secret", clientSecret).get().build()

        val res = client.newCall(req).execute()
        when {
            res.isSuccessful -> {
                gson.fromJson(res.body?.string() ?: "", NaverShoppingResponse::class.java) ?: NaverShoppingResponse()
            }
            res.code == 429 -> {
                // Rate limit 초과 → 재시도 대상
                throw RetryableApiException("네이버 API 429 Too Many Requests")
            }
            res.code in 500..599 -> {
                // 서버 일시 오류 → 재시도 대상
                throw RetryableApiException("네이버 API 서버 오류 ${res.code}")
            }
            else -> {
                // 401/403 등 인증 오류 → 재시도해도 의미 없으므로 빈 결과 즉시 반환
                NaverShoppingResponse()
            }
        }
    }
}
