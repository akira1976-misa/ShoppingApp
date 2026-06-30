package com.shopping.pricecompare.model
import java.io.Serializable

data class SellerListing(
    val shopName: String, val price: Int, val shippingFee: Int, val productUrl: String = ""
) : Serializable {
    val totalPrice: Int get() = price + shippingFee
    val isFreeShipping: Boolean get() = shippingFee == 0
}

data class Product(
    val id: Int, val name: String, val imageUrl: String, val category: String,
    val rating: Float, val reviewCount: Int, val description: String = "",
    val isSpecialDeal: Boolean = false, val discountRate: Int = 0,
    val sellers: List<SellerListing> = emptyList()
) : Serializable {
    val lowestSeller: SellerListing? get() = sellers.minByOrNull { it.totalPrice }
    val lowestTotalPrice: Int get() = lowestSeller?.totalPrice ?: 0
    val lowestPrice: Int get() = lowestSeller?.price ?: 0
    val lowestShipping: Int get() = lowestSeller?.shippingFee ?: 0
    val isFreeShipping: Boolean get() = lowestSeller?.isFreeShipping ?: false
    val originalPrice: Int get() = if (isSpecialDeal && discountRate > 0)
        (lowestPrice * 100 / (100 - discountRate)) else lowestPrice
}

enum class SortOption(val label: String) {
    LOWEST_TOTAL("최저가순"), PRICE_LOW("가격 낮은순"), PRICE_HIGH("가격 높은순"),
    REVIEW_COUNT("리뷰 많은순"), RATING("별점 높은순")
}
