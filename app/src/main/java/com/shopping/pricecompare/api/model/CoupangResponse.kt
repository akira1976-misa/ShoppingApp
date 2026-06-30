package com.shopping.pricecompare.api.model
data class CoupangSearchResponse(
    val rCode: String = "", val rMessage: String = "", val data: CoupangSearchData? = null
)
data class CoupangSearchData(val productData: List<CoupangProduct> = emptyList())
data class CoupangProduct(
    val productId: Long = 0, val productName: String = "", val productPrice: Int = 0,
    val productImage: String = "", val productUrl: String = "", val coupangUrl: String = "",
    val shopName: String = "쿠팡", val isRocket: Boolean = false, val freeShipping: Boolean = false
)
