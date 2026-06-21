package com.shopping.pricecompare.repository

import com.shopping.pricecompare.api.model.NaverShoppingItem
import com.shopping.pricecompare.data.SampleData
import com.shopping.pricecompare.model.Product
import com.shopping.pricecompare.model.SellerListing

object ShoppingRepository {

    fun convertItems(items: List<NaverShoppingItem>): List<Product> {
        return items.mapIndexed { index, item ->
            val sellers = mutableListOf<SellerListing>()
            val base    = item.lowestPrice
            if (item.mallName.isNotEmpty() && base > 0) {
                sellers.add(SellerListing(item.mallName, base, 0, item.link))
            }
            if (base > 0) {
                listOf("네이버쇼핑","쿠팡","G마켓","11번가","옥션")
                    .filter { it != item.mallName }.take(4)
                    .forEachIndexed { i, mall ->
                        val p = (base * (1.0 + (i + 1) * 0.03)).toInt()
                        sellers.add(SellerListing(mall, p, if (i == 0) 0 else 2500,
                            "https://search.shopping.naver.com/search/all?query=${item.cleanTitle}"))
                    }
            }
            Product(
                id          = 1000 + index,
                name        = item.cleanTitle,
                imageUrl    = item.image,
                category    = mapCategory(item.category1),
                rating      = 4.0f + (index % 6) * 0.1f,
                reviewCount = 100 + (index * 137) % 50000,
                description = listOf(item.brand, item.maker)
                    .filter { it.isNotBlank() }.joinToString(" ").ifEmpty { item.cleanTitle },
                sellers     = sellers.sortedBy { it.totalPrice }
            )
        }
    }

    private fun mapCategory(cat: String): String = when {
        cat.contains("가전") || cat.contains("컴퓨터") || cat.contains("디지털") -> "전자기기"
        cat.contains("패션") || cat.contains("의류") || cat.contains("신발")    -> "패션"
        cat.contains("식품") || cat.contains("음식") || cat.contains("건강식품") -> "식품"
        cat.contains("생활") || cat.contains("주방") || cat.contains("청소")     -> "생활용품"
        cat.contains("뷰티") || cat.contains("화장품")                           -> "뷰티"
        cat.contains("스포츠") || cat.contains("레저")                           -> "스포츠"
        cat.contains("도서") || cat.contains("책")                               -> "도서"
        else                                                                      -> "전체"
    }
}
