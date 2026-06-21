package com.shopping.pricecompare.data

object AutoCompleteData {

    /** 인기 검색어 (카테고리별) */
    private val popular = mapOf(
        "전자기기" to listOf("삼성 갤럭시 S24","아이폰 15 Pro","LG 그램 노트북",
            "애플 에어팟 프로","갤럭시 버즈2","소니 WH-1000XM5","삼성 QLED TV",
            "아이패드 에어","맥북 에어 M2","갤럭시 워치6"),
        "패션"    to listOf("나이키 에어맥스","아디다스 울트라부스트","노스페이스 패딩",
            "뉴발란스 992","컨버스 척테일러","롱샴 토트백","리바이스 청바지",
            "MLB 볼캡","나이키 조던","유니클로 히트텍"),
        "식품"    to listOf("비비고 만두","신라면 멀티팩","불닭볶음면","삼다수",
            "하림 닭가슴살","오뚜기 진라면","스타벅스 원두","동원 참치"),
        "생활용품" to listOf("다이슨 청소기","LG 공기청정기","쿠쿠 밥솥","락앤락",
            "테팔 프라이팬","코웨이 정수기","이케아 선반"),
        "뷰티"    to listOf("설화수 에센스","닥터자르트 시카페어","라네즈 마스크",
            "미샤 타임레볼루션","헤라 쿠션","메디힐 마스크팩","판테닌 샴푸"),
        "스포츠"  to listOf("가민 스마트워치","요가매트","나이키 러닝화","덤벨 세트",
            "마이프로틴 단백질","아식스 젤카야노","골프채"),
        "도서"    to listOf("채식주의자","불편한 편의점","역행자","돈의 심리학",
            "클린 코드","미라클 모닝","82년생 김지영")
    )

    /** 전체 인기 검색어 */
    val allPopular: List<String> = popular.values.flatten().distinct()

    /** 브랜드 목록 */
    val brands = listOf(
        "삼성","LG","애플","소니","나이키","아디다스","유니클로","뉴발란스",
        "노스페이스","컨버스","MLB","다이슨","필립스","쿠팡","쿠쿠","테팔",
        "설화수","라네즈","미샤","닥터자르트","가민","마이프로틱"
    )

    /**
     * 입력 문자열에 맞는 자동완성 후보 반환
     * @param input 입력 문자열
     * @param category 현재 카테고리 (optional)
     */
    fun getSuggestions(input: String, category: String = ""): List<String> {
        if (input.length < 1) return emptyList()
        val q = input.trim().lowercase()
        val pool = if (category.isNotEmpty() && popular.containsKey(category))
            (popular[category]!! + allPopular).distinct()
        else allPopular

        return pool.filter { it.lowercase().contains(q) }.take(8)
    }

    /**
     * 검색어에 대한 연관 검색어 반환
     */
    fun getRelated(query: String): List<String> {
        val q = query.trim().lowercase()
        val related = mutableListOf<String>()

        // 브랜드 기반 연관어
        brands.forEach { brand ->
            if (q.contains(brand.lowercase())) {
                related.addAll(allPopular.filter {
                    it.lowercase().contains(brand.lowercase()) && it.lowercase() != q
                })
            }
        }

        // 카테고리 기반 연관어
        popular.forEach { (_, terms) ->
            if (terms.any { it.lowercase().contains(q) }) {
                related.addAll(terms.filter { it.lowercase() != q })
            }
        }

        return related.distinct().take(8)
    }
}
