package com.shopping.pricecompare.api

import com.google.gson.Gson
import com.shopping.pricecompare.BuildConfig
import com.shopping.pricecompare.api.model.CoupangProduct
import com.shopping.pricecompare.api.model.CoupangSearchResponse
import com.shopping.pricecompare.util.RateLimiter
import com.shopping.pricecompare.util.RetryPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * /skill-crawler: 쿠팡 파트너스 API 연동.
 * /skill-routing: subId로 수익화 추적.
 * - Timeout 설정 (무한 로딩 방지)
 * - Rate Limiter + Retry 적용
 */
object CoupangPartnerService {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL    = "https://api-gateway.coupang.com"
    private const val SEARCH_PATH = "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search"
    private const val AFFILIATE_SUB_ID = "shopping_app"

    private val rateLimiter = RateLimiter(maxCalls = 5, windowMs = 1_000L)

    private class RetryableApiException(msg: String) : IOException(msg)

    suspend fun search(keyword: String, limit: Int = 20): List<CoupangProduct> =
        withContext(Dispatchers.IO) {
            val ak = BuildConfig.COUPANG_ACCESS_KEY
            val sk = BuildConfig.COUPANG_SECRET_KEY
            if (ak.isEmpty() || sk.isEmpty()) return@withContext emptyList()

            try {
                RetryPolicy.withRetry(
                    maxAttempts = 3,
                    initialDelayMs = 300L,
                    shouldRetry = { e -> e is RetryableApiException }
                ) {
                    rateLimiter.acquire()
                    fetchRaw(keyword, limit, ak, sk)
                }
            } catch (e: Exception) { emptyList() }
        }

    private fun fetchRaw(keyword: String, limit: Int, ak: String, sk: String): List<CoupangProduct> {
        val q        = URLEncoder.encode(keyword, "UTF-8")
        val query    = "keyword=$q&limit=$limit&subId=$AFFILIATE_SUB_ID"
        val fullPath = "$SEARCH_PATH?$query"
        val dt       = getDatetime()
        val sig      = hmac(sk, "GET", fullPath, dt)
        val auth     = "CEA algorithm=HmacSHA256, access-key=$ak, signed-date=$dt, signature=$sig"

        val req = Request.Builder().url("$BASE_URL$fullPath")
            .addHeader("Authorization", auth)
            .addHeader("Content-Type", "application/json;charset=UTF-8")
            .get().build()

        val res = client.newCall(req).execute()
        return when {
            res.isSuccessful ->
                gson.fromJson(res.body?.string() ?: "", CoupangSearchResponse::class.java)
                    ?.data?.productData ?: emptyList()
            res.code == 429 || res.code in 500..599 ->
                throw RetryableApiException("쿠팡 API 오류 ${res.code}")
            else -> emptyList()
        }
    }

    private fun hmac(sk: String, method: String, path: String, dt: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(sk.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal("$dt$method$path".toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun getDatetime(): String {
        val sdf = SimpleDateFormat("yyMMdd'T'HHmmss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
