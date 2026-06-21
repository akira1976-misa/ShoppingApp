package com.shopping.pricecompare.api.model
data class NaverShoppingResponse(
    val total: Int = 0,
    val start: Int = 0,
    val display: Int = 0,
    val items: List<NaverShoppingItem> = emptyList()
)
data class NaverShoppingItem(
    val title: String = "",
    val link: String = "",
    val image: String = "",
    val lprice: String = "0",
    val hprice: String = "0",
    val mallName: String = "",
    val productId: String = "",
    val productType: String = "2",
    val brand: String = "",
    val maker: String = "",
    val category1: String = "",
    val category2: String = "",
    val category3: String = "",
    val category4: String = ""
) {
    val cleanTitle: String get() = title.replace(Regex("<[^>]*>"), "")
    val lowestPrice: Int get() = lprice.toIntOrNull() ?: 0
    val category: String get() = when {
        category4.isNotEmpty() -> category4
        category3.isNotEmpty() -> category3
        category2.isNotEmpty() -> category2
        else -> category1
    }
}
