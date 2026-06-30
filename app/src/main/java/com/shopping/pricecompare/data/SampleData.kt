package com.shopping.pricecompare.data

import com.shopping.pricecompare.model.Product
import com.shopping.pricecompare.model.SellerListing

object SampleData {
    val categories = listOf("전체","전자기기","패션","식품","생활용품","뷰티","스포츠","도서")

    val products: List<Product> = listOf(
        Product(1,"삼성 갤럭시 S24 Ultra 256GB","https://picsum.photos/seed/s24u/400/400",
            "전자기기",4.7f,5821,"6.8인치 AMOLED, S펜 내장, 200MP 카메라",true,15,listOf(
            SellerListing("쿠팡",1419000,0,"https://www.coupang.com/np/search?q=갤럭시S24Ultra"),
            SellerListing("네이버쇼핑",1409000,0,"https://search.shopping.naver.com/search/all?query=갤럭시S24Ultra"),
            SellerListing("G마켓",1435000,0,"https://browse.gmarket.co.kr/search?keyword=갤럭시S24Ultra"),
            SellerListing("11번가",1429000,0,"https://m.11st.co.kr/search/Search.tmall?searchKeyword=갤럭시S24Ultra"))),
        Product(2,"애플 아이폰 15 Pro 128GB","https://picsum.photos/seed/ip15pro/400/400",
            "전자기기",4.8f,9240,"티타늄 디자인, A17 Pro 칩, 48MP 카메라",false,0,listOf(
            SellerListing("쿠팡",1550000,0,"https://www.coupang.com/np/search?q=아이폰15Pro"),
            SellerListing("네이버쇼핑",1555000,0,"https://m.shopping.naver.com/search/all?query=아이폰15Pro"),
            SellerListing("Apple KR",1550000,0,"https://www.apple.com/kr"))),
        Product(3,"삼성 갤럭시 버즈2 프로","https://picsum.photos/seed/buds2p/400/400",
            "전자기기",4.5f,3241,"ANC, 360° 오디오, 최대 29시간",true,36,listOf(
            SellerListing("쿠팡",87500,0,"https://www.coupang.com/np/search?q=갤럭시버즈2프로"),
            SellerListing("네이버쇼핑",88000,0,"https://m.shopping.naver.com/search/all?query=갤럭시버즈2프로"),
            SellerListing("삼성공식몰",89000,0,"https://www.samsung.com/sec"))),
        Product(4,"애플 에어팟 프로 2세대","https://picsum.photos/seed/app2/400/400",
            "전자기기",4.8f,8750,"H2 칩, 향상된 ANC, USB-C 충전",false,0,listOf(
            SellerListing("쿠팡",239000,0,"https://www.coupang.com/np/search?q=에어팟프로2"),
            SellerListing("Apple KR",249000,0,"https://www.apple.com/kr"))),
        Product(5,"나이키 에어맥스 270","https://picsum.photos/seed/nm270/400/400",
            "패션",4.6f,5830,"최대 에어 유닛, 편안한 착용감",true,40,listOf(
            SellerListing("쿠팡",75000,3000,"https://www.coupang.com/np/search?q=나이키에어맥스270"),
            SellerListing("무신사",72000,0,"https://www.musinsa.com/search/musinsa/integration?q=나이키에어맥스270"))),
        Product(6,"유니클로 히트텍 긴팔 티셔츠","https://picsum.photos/seed/httech/400/400",
            "패션",4.7f,12340,"발열 소재, 신축성 우수",false,0,listOf(
            SellerListing("쿠팡",21000,0,"https://www.coupang.com/np/search?q=유니클로히트텍"),
            SellerListing("유니클로",19900,0,"https://www.uniqlo.com/kr"))),
        Product(7,"CJ 비비고 왕교자 만두 1.2kg","https://picsum.photos/seed/bibigo/400/400",
            "식품",4.8f,28750,"국내산 돼지고기와 부추",true,20,listOf(
            SellerListing("쿠팡",7900,0,"https://www.coupang.com/np/search?q=비비고만두"),
            SellerListing("마켓컬리",8500,0,"https://www.kurly.com/search?sword=비비고만두"))),
        Product(8,"농심 신라면 멀티팩 120g×20개","https://picsum.photos/seed/shinra/400/400",
            "식품",4.9f,45120,"매콤한 국물, 대한민국 1위 라면",false,0,listOf(
            SellerListing("쿠팡",14500,0,"https://www.coupang.com/np/search?q=신라면멀티팩"),
            SellerListing("마켓컬리",15000,0,"https://www.kurly.com/search?sword=신라면멀티팩"))),
        Product(9,"다이슨 V12 코드리스 청소기","https://picsum.photos/seed/dyv12/400/400",
            "생활용품",4.7f,2340,"레이저 먼지 감지, 강력 흡입력",false,0,listOf(
            SellerListing("쿠팡",559000,0,"https://www.coupang.com/np/search?q=다이슨V12"),
            SellerListing("다이슨공식",549000,0,"https://www.dyson.co.kr"))),
        Product(10,"닥터자르트 시카페어 크림 50ml","https://picsum.photos/seed/djcica/400/400",
            "뷰티",4.7f,14530,"민감 피부 진정, 병풀 성분 함유",true,27,listOf(
            SellerListing("쿠팡",22500,0,"https://www.coupang.com/np/search?q=닥터자르트시카페어"),
            SellerListing("올리브영",21900,0,"https://www.oliveyoung.co.kr/store/search/getSearchList.do?query=닥터자르트시카페어"))),
        Product(11,"가민 포러너 55 GPS 스마트워치","https://picsum.photos/seed/grmn55/400/400",
            "스포츠",4.6f,2980,"GPS, 심박수 모니터링",false,0,listOf(
            SellerListing("쿠팡",193000,0,"https://www.coupang.com/np/search?q=가민포러너55"),
            SellerListing("가민공식",189000,0,"https://www.garmin.com/ko-KR"))),
        Product(12,"불편한 편의점 (김호연)","https://picsum.photos/seed/bkconv/400/400",
            "도서",4.8f,32450,"2022 밀리언셀러, 따뜻한 힐링 소설",false,0,listOf(
            SellerListing("쿠팡",13000,0,"https://www.coupang.com/np/search?q=불편한편의점"),
            SellerListing("교보문고",13500,0,"https://search.kyobobook.co.kr/search?keyword=불편한편의점"))),
        Product(13,"LG 그램 14 노트북 i5/16GB/512GB","https://picsum.photos/seed/lgg14/400/400",
            "전자기기",4.6f,1823,"1.35kg 초경량, 16시간 배터리",true,15,listOf(
            SellerListing("쿠팡",1199000,0,"https://www.coupang.com/np/search?q=LG그램14"),
            SellerListing("LG공식몰",1189000,0,"https://www.lge.co.kr"))),
        Product(14,"소니 WH-1000XM5 헤드폰","https://picsum.photos/seed/xm5/400/400",
            "전자기기",4.7f,4120,"업계 최고 노이즈캔슬링",true,20,listOf(
            SellerListing("쿠팡",319000,0,"https://www.coupang.com/np/search?q=소니XM5"),
            SellerListing("소니스토어",349000,0,"https://store.sony.co.kr")))
    )

    fun getByCategory(cat: String) = if (cat == "전체") products else products.filter { it.category == cat }
    fun getSpecialDeals() = products.filter { it.isSpecialDeal }
    fun getHotDealsByCategory(cat: String, limit: Int = 5): List<Product> {
        val list = if (cat == "전체") products else products.filter { it.category == cat }
        val deals = list.filter { it.isSpecialDeal }.sortedByDescending { it.discountRate }
        return (deals + list.sortedBy { it.lowestTotalPrice }).distinct().take(limit)
    }
    fun search(q: String): List<Product> {
        if (q.isBlank()) return products
        val query = q.trim().lowercase()
        return products.filter { p ->
            p.name.lowercase().contains(query) || p.category.lowercase().contains(query) ||
            p.description.lowercase().contains(query) || p.sellers.any { it.shopName.lowercase().contains(query) }
        }
    }
}
