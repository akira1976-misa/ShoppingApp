package com.shopping.pricecompare.api

import com.google.gson.Gson
import com.shopping.pricecompare.BuildConfig
import com.shopping.pricecompare.api.model.NaverShoppingResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.net.URLEncoder

object NaverShoppingService {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }).build()

    data class PagedResult(
        val items: List<com.shopping.pricecompare.api.model.NaverShoppingItem>,
        val totalCount: Int,
        val currentPage: Int,
        val pageSize: Int
    ) {
        val totalPages: Int get() =
            if (totalCount <= 0) 1
            else minOf(8, (totalCount + pageSize - 1) / pageSize)
        val hasNext: Boolean get() = currentPage < totalPages
        val hasPrev: Boolean get() = currentPage > 1
    }

    suspend fun searchPaged(
        query: String,
        page: Int = 1,
        pageSize: Int = 120,
        sort: String = "sim"
    ): PagedResult = withContext(Dispatchers.IO) {
        val clientId     = BuildConfig.NAVER_CLIENT_ID
        val clientSecret = BuildConfig.NAVER_CLIENT_SECRET
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            return@withContext PagedResult(emptyList(), 0, page, pageSize)
        }
        val startIndex = (page - 1) * pageSize + 1
        val items = mutableListOf<com.shopping.pricecompare.api.model.NaverShoppingItem>()
        val r1 = fetchRaw(query, minOf(pageSize, 100), startIndex, sort, clientId, clientSecret)
        items.addAll(r1.items)
        val remaining = pageSize - items.size
        if (remaining > 0 && startIndex + 100 <= 1000) {
            val r2 = fetchRaw(query, remaining, startIndex + 100, sort, clientId, clientSecret)
            items.addAll(r2.items)
        }
        PagedResult(items, r1.total, page, pageSize)
    }

    private suspend fun fetchRaw(
        query: String, display: Int, start: Int, sort: String,
        clientId: String, clientSecret: String
    ): NaverShoppingResponse = withContext(Dispatchers.IO) {
        val q   = URLEncoder.encode(query, "UTF-8")
        val url = "https://openapi.naver.com/v1/search/shop.json?query=$q&display=$display&start=$start&sort=$sort"
        val req = Request.Builder().url(url)
            .addHeader("X-Naver-Client-Id", clientId)
            .addHeader("X-Naver-Client-Secret", clientSecret)
            .get().build()
        return@withContext try {
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                gson.fromJson(res.body?.string() ?: "", NaverShoppingResponse::class.java)
                    ?: NaverShoppingResponse()
            } else NaverShoppingResponse()
        } catch (e: Exception) { NaverShoppingResponse() }
    }
}
