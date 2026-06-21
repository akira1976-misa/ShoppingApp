package com.shopping.pricecompare.api

import com.google.gson.Gson
import com.shopping.pricecompare.BuildConfig
import com.shopping.pricecompare.api.model.CoupangProduct
import com.shopping.pricecompare.api.model.CoupangSearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CoupangPartnerService {
    private val gson   = Gson()
    private val client = OkHttpClient.Builder().build()
    private const val BASE_URL    = "https://api-gateway.coupang.com"
    private const val SEARCH_PATH = "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search"

    suspend fun search(keyword: String, limit: Int = 20): List<CoupangProduct> =
        withContext(Dispatchers.IO) {
            val ak = BuildConfig.COUPANG_ACCESS_KEY
            val sk = BuildConfig.COUPANG_SECRET_KEY
            if (ak.isEmpty() || sk.isEmpty()) return@withContext emptyList()
            val q        = URLEncoder.encode(keyword, "UTF-8")
            val query    = "keyword=$q&limit=$limit&subId=shopping_app"
            val fullPath = "$SEARCH_PATH?$query"
            val dt       = getDatetime()
            val sig      = hmac(sk, "GET", fullPath, dt)
            val auth     = "CEA algorithm=HmacSHA256, access-key=$ak, signed-date=$dt, signature=$sig"
            val req      = Request.Builder()
                .url("$BASE_URL$fullPath")
                .addHeader("Authorization", auth)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .get().build()
            return@withContext try {
                val res = client.newCall(req).execute()
                if (res.isSuccessful)
                    gson.fromJson(res.body?.string() ?: "", CoupangSearchResponse::class.java)
                        ?.data?.productData ?: emptyList()
                else emptyList()
            } catch (e: Exception) { emptyList() }
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
