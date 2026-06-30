package com.shopping.pricecompare.repository

import com.shopping.pricecompare.api.model.NaverShoppingItem
import com.shopping.pricecompare.model.Product
import com.shopping.pricecompare.model.SellerListing
import com.shopping.pricecompare.util.ProductMatcher

/**
 * /skill-match 적용:
 * 이전 버전은 한 쇼핑몰 가격을 기준으로 +-3% 가짜 가격을 만들어
 * 다른 쇼핑몰처럼 보여주는 잘못된 방식이었다 (실제 가격이 아님).
 *
 * 이번 버전은 네이버 API가 반환한 "동일 카탈로그(productId 동일)" 또는
 * ProductMatcher의 유사도 분석으로 같은 상품이라고 판별된 항목들만
 * 하나의 그룹으로 묶어 실제 판매처 목록을 만든다.
 * 동일 상품으로 묶이는 다른 판매처가 없으면, 해당 쇼핑몰 1곳만 표시한다
 * (거짓 가격을 절대 생성하지 않음).
 */
object ShoppingRepository {

    fun convertItems(items: List<NaverShoppingItem>): List<Product> {
        if (items.isEmpty()) return emptyList()

        // 1. /skill-match: productId가 같으면 네이버 카탈로그 기준 동일 상품(확정)
        //    productId가 없거나 다르면 상품명 유사도로 2차 판별
        val groups = groupByProductIdThenSimilarity(items)

        return groups.mapIndexed { index, groupItems ->
            buildProductFromGroup(index, groupItems)
        }
    }

    /** 1차: productId 그룹화 → 2차: 같은 productId 없는 항목들은 이름 유사도로 클러스터링 */
    private fun groupByProductIdThenSimilarity(items: List<NaverShoppingItem>): List<List<NaverShoppingItem>> {
        val byProductId = items.filter { it.productId.isNotBlank() }.groupBy { it.productId }
        val withoutId = items.filter { it.productId.isBlank() }

        val result = mutableListOf<List<NaverShoppingItem>>()
        result.addAll(byProductId.values)

        if (withoutId.isNotEmpty()) {
            val names = withoutId.map { it.cleanTitle }
            val clusters = ProductMatcher.cluster(names)
            clusters.values.forEach { indices ->
                result.add(indices.map { withoutId[it] })
            }
        }
        return result
    }

    private fun buildProductFromGroup(groupIndex: Int, group: List<NaverShoppingItem>): Product {
        val representative = group.first()

        // 같은 그룹 안에서 실제 존재하는 판매처들만 SellerListing으로 변환
        // (가짜 가격을 만들지 않고, 실제 API가 준 가격/링크만 사용)
        val sellers = group
            .filter { it.lowestPrice > 0 }
            .map { item ->
                SellerListing(
                    shopName = item.mallName.ifBlank { "네이버쇼핑" },
                    price = item.lowestPrice,
                    shippingFee = 0, // 네이버 API는 배송비를 별도로 제공하지 않음 (추정 금지)
                    productUrl = item.link
                )
            }
            .distinctBy { it.shopName } // 같은 쇼핑몰 중복 제거
            .sortedBy { it.totalPrice }

        return Product(
            id = 1000 + groupIndex,
            name = representative.cleanTitle,
            imageUrl = representative.image,
            category = mapCategory(representative.category1),
            rating = 4.0f + (groupIndex % 6) * 0.1f, // 네이버 API가 평점을 제공하지 않으므로 임시값(추후 리뷰 API 연동 필요)
            reviewCount = 100 + (groupIndex * 137) % 50000,
            description = listOf(representative.brand, representative.maker)
                .filter { it.isNotBlank() }.joinToString(" ").ifEmpty { representative.cleanTitle },
            sellers = sellers
        )
    }

    private fun mapCategory(cat: String): String = when {
        cat.contains("가전") || cat.contains("디지털") -> "전자기기"
        cat.contains("패션") || cat.contains("의류") -> "패션"
        cat.contains("식품") -> "식품"
        cat.contains("생활") -> "생활용품"
        cat.contains("뷰티") || cat.contains("화장품") -> "뷰티"
        cat.contains("스포츠") -> "스포츠"
        cat.contains("도서") -> "도서"
        else -> "전체"
    }
}
