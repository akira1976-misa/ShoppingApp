package com.shopping.pricecompare.model

import java.io.Serializable

data class Product(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val price: Int,           // 상품가격 (원)
    val shippingFee: Int,     // 배송비 (0 = 무료배송)
    val rating: Float,        // 별점 (0.0 ~ 5.0)
    val reviewCount: Int,     // 리뷰 개수
    val category: String,     // 카테고리
    val shopName: String,     // 판매처
    val isSpecialDeal: Boolean = false,  // 특가 여부
    val discountRate: Int = 0,           // 할인율 (%)
    val originalPrice: Int = 0,          // 정가
    val productUrl: String = "",         // 상품 페이지 URL
    val description: String = ""         // 상품 설명
) : Serializable {
    /** 총 비용 = 상품가격 + 배송비 */
    val totalPrice: Int get() = price + shippingFee

    /** 무료배송 여부 */
    val isFreeShipping: Boolean get() = shippingFee == 0

    /** 절약금액 (특가인 경우) */
    val savedAmount: Int get() = if (isSpecialDeal && originalPrice > 0) originalPrice - price else 0
}

/** 정렬 기준 */
enum class SortOption(val label: String) {
    LOWEST_TOTAL("최저가순 (배송비 포함)"),
    PRICE_LOW("가격 낮은순"),
    PRICE_HIGH("가격 높은순"),
    REVIEW_COUNT("리뷰 많은순"),
    RATING("별점 높은순")
}
