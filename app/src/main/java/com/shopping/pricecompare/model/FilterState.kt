package com.shopping.pricecompare.model

enum class ShippingType(val label: String) {
    ALL("전체"),
    FREE("무료배송"),
    DOMESTIC("국내배송"),
    OVERSEAS("해외배송")
}

data class FilterState(
    val shippingType   : ShippingType = ShippingType.ALL,
    val minPrice       : Int          = 0,
    val maxPrice       : Int          = Int.MAX_VALUE,
    val selectedBrands : Set<String>  = emptySet()
) {
    /** 활성화된 필터 수 (배지에 표시) */
    val activeCount: Int get() = listOfNotNull(
        if (shippingType != ShippingType.ALL) 1 else null,
        if (minPrice > 0 || maxPrice < Int.MAX_VALUE) 1 else null,
        if (selectedBrands.isNotEmpty()) 1 else null
    ).size

    val isActive: Boolean get() = activeCount > 0

    companion object {
        /** 가격대 프리셋 (라벨, 최소, 최대) */
        val PRICE_PRESETS = listOf(
            Triple("전체",         0,          Int.MAX_VALUE),
            Triple("1만원 이하",   0,          10_000),
            Triple("1~5만원",      10_000,     50_000),
            Triple("5~10만원",     50_000,     100_000),
            Triple("10~30만원",    100_000,    300_000),
            Triple("30~100만원",   300_000,    1_000_000),
            Triple("100만원 이상", 1_000_000,  Int.MAX_VALUE)
        )
    }
}
