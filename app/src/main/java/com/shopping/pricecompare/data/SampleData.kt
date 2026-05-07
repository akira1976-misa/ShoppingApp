package com.shopping.pricecompare.data

import com.shopping.pricecompare.model.Product

object SampleData {

    val categories = listOf(
        "전체", "전자기기", "패션", "식품", "생활용품", "뷰티", "스포츠", "도서"
    )

    val products = listOf(
        // ── 전자기기 ──────────────────────────────────────────────────
        Product(
            id = 1,
            name = "삼성 갤럭시 버즈2 프로 무선 이어폰",
            imageUrl = "https://picsum.photos/seed/earbuds1/400/400",
            price = 89000,
            shippingFee = 0,
            rating = 4.5f,
            reviewCount = 3241,
            category = "전자기기",
            shopName = "삼성 공식몰",
            isSpecialDeal = true,
            discountRate = 36,
            originalPrice = 139000,
            productUrl = "https://www.samsung.com/sec/",
            description = "최신 ANC 기능과 360° 오디오를 지원하는 프리미엄 무선 이어폰"
        ),
        Product(
            id = 2,
            name = "애플 에어팟 프로 2세대",
            imageUrl = "https://picsum.photos/seed/airpods2/400/400",
            price = 239000,
            shippingFee = 0,
            rating = 4.8f,
            reviewCount = 8750,
            category = "전자기기",
            shopName = "Apple Korea",
            isSpecialDeal = false,
            productUrl = "https://www.apple.com/kr/",
            description = "H2 칩 탑재, 향상된 Active Noise Cancellation"
        ),
        Product(
            id = 3,
            name = "LG 그램 14 노트북 (i5/16GB/512GB)",
            imageUrl = "https://picsum.photos/seed/laptop3/400/400",
            price = 1189000,
            shippingFee = 0,
            rating = 4.6f,
            reviewCount = 1823,
            category = "전자기기",
            shopName = "LG전자 공식몰",
            isSpecialDeal = true,
            discountRate = 15,
            originalPrice = 1390000,
            productUrl = "https://www.lge.co.kr/",
            description = "1.35kg 초경량, 16시간 배터리, MIL-SPEC 인증"
        ),
        Product(
            id = 4,
            name = "샤오미 로봇청소기 S10+",
            imageUrl = "https://picsum.photos/seed/vacuum4/400/400",
            price = 279000,
            shippingFee = 3000,
            rating = 4.3f,
            reviewCount = 962,
            category = "전자기기",
            shopName = "샤오미 공식스토어",
            isSpecialDeal = true,
            discountRate = 30,
            originalPrice = 399000,
            productUrl = "https://www.mi.com/kr",
            description = "레이저 네비게이션, 4000Pa 강력흡입"
        ),
        Product(
            id = 5,
            name = "필립스 에어프라이어 HD9252 (4.1L)",
            imageUrl = "https://picsum.photos/seed/airfryer5/400/400",
            price = 89900,
            shippingFee = 0,
            rating = 4.4f,
            reviewCount = 4112,
            category = "생활용품",
            shopName = "필립스 공식몰",
            productUrl = "https://www.philips.co.kr/",
            description = "Rapid Air 기술, 90% 적은 기름 사용"
        ),

        // ── 패션 ───────────────────────────────────────────────────────
        Product(
            id = 6,
            name = "나이키 에어맥스 270 운동화",
            imageUrl = "https://picsum.photos/seed/nike6/400/400",
            price = 79000,
            shippingFee = 0,
            rating = 4.6f,
            reviewCount = 5830,
            category = "패션",
            shopName = "나이키 코리아",
            isSpecialDeal = true,
            discountRate = 40,
            originalPrice = 132000,
            productUrl = "https://www.nike.com/kr/",
            description = "최대 에어 유닛으로 하루 종일 편안한 착용감"
        ),
        Product(
            id = 7,
            name = "아디다스 클래식 후드티 (남녀공용)",
            imageUrl = "https://picsum.photos/seed/adidas7/400/400",
            price = 49000,
            shippingFee = 3000,
            rating = 4.2f,
            reviewCount = 2140,
            category = "패션",
            shopName = "아디다스 공식몰",
            productUrl = "https://www.adidas.co.kr/",
            description = "트레포일 로고 기본 후드 스웨트셔츠"
        ),
        Product(
            id = 8,
            name = "유니클로 히트텍 긴팔 티셔츠",
            imageUrl = "https://picsum.photos/seed/uniqlo8/400/400",
            price = 19900,
            shippingFee = 0,
            rating = 4.7f,
            reviewCount = 12340,
            category = "패션",
            shopName = "유니클로",
            isSpecialDeal = false,
            productUrl = "https://www.uniqlo.com/kr/",
            description = "발열 소재로 겨울 체온 유지, 신축성 뛰어남"
        ),
        Product(
            id = 9,
            name = "리바이스 511 슬림 청바지",
            imageUrl = "https://picsum.photos/seed/levi9/400/400",
            price = 67000,
            shippingFee = 0,
            rating = 4.4f,
            reviewCount = 3980,
            category = "패션",
            shopName = "리바이스 공식몰",
            isSpecialDeal = true,
            discountRate = 25,
            originalPrice = 89000,
            productUrl = "https://www.levi.com/KR/ko_KR/",
            description = "슬림 핏 5포켓 진, 스트레치 소재"
        ),

        // ── 식품 ───────────────────────────────────────────────────────
        Product(
            id = 10,
            name = "CJ 비비고 만두 1.2kg (왕교자)",
            imageUrl = "https://picsum.photos/seed/food10/400/400",
            price = 8900,
            shippingFee = 3000,
            rating = 4.8f,
            reviewCount = 28750,
            category = "식품",
            shopName = "CJ 더마켓",
            isSpecialDeal = true,
            discountRate = 20,
            originalPrice = 11000,
            productUrl = "https://www.cjthemarket.com/",
            description = "국내산 돼지고기와 부추의 황금 비율, 1위 만두"
        ),
        Product(
            id = 11,
            name = "스타벅스 원두 파이크 플레이스 200g",
            imageUrl = "https://picsum.photos/seed/coffee11/400/400",
            price = 16900,
            shippingFee = 0,
            rating = 4.5f,
            reviewCount = 6230,
            category = "식품",
            shopName = "스타벅스 at Home",
            productUrl = "https://www.starbucksathome.com/",
            description = "부드럽고 균형 잡힌 맛의 미디엄 로스팅"
        ),
        Product(
            id = 12,
            name = "농심 신라면 멀티팩 120g×20개",
            imageUrl = "https://picsum.photos/seed/ramen12/400/400",
            price = 14500,
            shippingFee = 3000,
            rating = 4.9f,
            reviewCount = 45120,
            category = "식품",
            shopName = "농심 공식몰",
            productUrl = "https://www.nongshimusa.com/",
            description = "매콤한 국물의 대한민국 대표 라면"
        ),

        // ── 생활용품 ───────────────────────────────────────────────────
        Product(
            id = 13,
            name = "다이슨 V12 코드리스 청소기",
            imageUrl = "https://picsum.photos/seed/dyson13/400/400",
            price = 549000,
            shippingFee = 0,
            rating = 4.7f,
            reviewCount = 2340,
            category = "생활용품",
            shopName = "다이슨 공식몰",
            isSpecialDeal = false,
            productUrl = "https://www.dyson.co.kr/",
            description = "레이저로 먼지 감지, 초당 125,000번 회전 모터"
        ),
        Product(
            id = 14,
            name = "락앤락 유리 반찬통 세트 (10개)",
            imageUrl = "https://picsum.photos/seed/locknlock14/400/400",
            price = 29900,
            shippingFee = 0,
            rating = 4.5f,
            reviewCount = 8910,
            category = "생활용품",
            shopName = "락앤락 공식몰",
            isSpecialDeal = true,
            discountRate = 33,
            originalPrice = 44900,
            productUrl = "https://www.locknlock.com/",
            description = "내열유리 소재, 전자레인지·오븐 사용 가능"
        ),
        Product(
            id = 15,
            name = "퓨리케어 360 공기청정기 (48㎡)",
            imageUrl = "https://picsum.photos/seed/purifier15/400/400",
            price = 319000,
            shippingFee = 0,
            rating = 4.6f,
            reviewCount = 3120,
            category = "생활용품",
            shopName = "LG전자 공식몰",
            isSpecialDeal = true,
            discountRate = 20,
            originalPrice = 399000,
            productUrl = "https://www.lge.co.kr/",
            description = "360° 청정, 초극세사 필터, AI 자동 운전"
        ),

        // ── 뷰티 ───────────────────────────────────────────────────────
        Product(
            id = 16,
            name = "설화수 자음 2호 에센스 (60ml)",
            imageUrl = "https://picsum.photos/seed/beauty16/400/400",
            price = 89000,
            shippingFee = 0,
            rating = 4.6f,
            reviewCount = 7820,
            category = "뷰티",
            shopName = "설화수 공식몰",
            isSpecialDeal = false,
            productUrl = "https://www.sulwhasoo.com/kr/",
            description = "자음단 성분으로 피부 탄력·수분 케어"
        ),
        Product(
            id = 17,
            name = "닥터자르트 시카페어 크림 (50ml)",
            imageUrl = "https://picsum.photos/seed/drjar17/400/400",
            price = 21900,
            shippingFee = 0,
            rating = 4.7f,
            reviewCount = 14530,
            category = "뷰티",
            shopName = "올리브영",
            isSpecialDeal = true,
            discountRate = 27,
            originalPrice = 30000,
            productUrl = "https://www.oliveyoung.co.kr/",
            description = "민감 피부 진정, 병원성 성분 함유 진정 크림"
        ),
        Product(
            id = 18,
            name = "아이오페 레티놀 엑스퍼트 0.1%",
            imageUrl = "https://picsum.photos/seed/iope18/400/400",
            price = 38000,
            shippingFee = 0,
            rating = 4.4f,
            reviewCount = 5610,
            category = "뷰티",
            shopName = "아이오페 공식몰",
            productUrl = "https://www.iope.com/kr/",
            description = "순수 레티놀 0.1% 함유, 주름 개선 기능성"
        ),

        // ── 스포츠 ─────────────────────────────────────────────────────
        Product(
            id = 19,
            name = "알톤 접이식 자전거 20인치 (7단)",
            imageUrl = "https://picsum.photos/seed/bike19/400/400",
            price = 179000,
            shippingFee = 5000,
            rating = 4.3f,
            reviewCount = 1840,
            category = "스포츠",
            shopName = "알톤 공식몰",
            isSpecialDeal = true,
            discountRate = 28,
            originalPrice = 249000,
            productUrl = "https://www.alton.co.kr/",
            description = "경량 알루미늄 프레임, 시마노 7단 변속"
        ),
        Product(
            id = 20,
            name = "요가매트 TPE 8mm 논슬립",
            imageUrl = "https://picsum.photos/seed/yoga20/400/400",
            price = 24900,
            shippingFee = 0,
            rating = 4.5f,
            reviewCount = 6720,
            category = "스포츠",
            shopName = "스포츠다이렉트",
            productUrl = "https://www.sportsdirect.com/",
            description = "친환경 TPE 소재, 양면 논슬립 텍스처"
        ),
        Product(
            id = 21,
            name = "가민 포러너 55 GPS 스마트워치",
            imageUrl = "https://picsum.photos/seed/garmin21/400/400",
            price = 189000,
            shippingFee = 0,
            rating = 4.6f,
            reviewCount = 2980,
            category = "스포츠",
            shopName = "가민 공식몰",
            isSpecialDeal = false,
            productUrl = "https://www.garmin.com/ko-KR/",
            description = "GPS 내장, 심박수 모니터링, 20가지 운동 모드"
        ),

        // ── 도서 ───────────────────────────────────────────────────────
        Product(
            id = 22,
            name = "불편한 편의점 (김호연 장편소설)",
            imageUrl = "https://picsum.photos/seed/book22/400/400",
            price = 13500,
            shippingFee = 0,
            rating = 4.8f,
            reviewCount = 32450,
            category = "도서",
            shopName = "예스24",
            isSpecialDeal = false,
            productUrl = "https://www.yes24.com/",
            description = "2022 올해의 책, 밀리언셀러 소설"
        ),
        Product(
            id = 23,
            name = "코틀린 인 액션 (한국어판)",
            imageUrl = "https://picsum.photos/seed/book23/400/400",
            price = 36000,
            shippingFee = 0,
            rating = 4.7f,
            reviewCount = 1230,
            category = "도서",
            shopName = "교보문고",
            isSpecialDeal = true,
            discountRate = 10,
            originalPrice = 40000,
            productUrl = "https://www.kyobobook.co.kr/",
            description = "Kotlin 공식 도서, JetBrains 개발자 저술"
        )
    )

    fun getByCategory(category: String): List<Product> {
        return if (category == "전체") products else products.filter { it.category == category }
    }

    fun search(query: String): List<Product> {
        if (query.isBlank()) return products
        return products.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            it.shopName.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }
    }

    fun getSpecialDeals(): List<Product> = products.filter { it.isSpecialDeal }
}
