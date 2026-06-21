package com.shopping.pricecompare.data

data class SubCategory(val id: String, val name: String, val query: String)
data class MidCategory(val id: String, val name: String, val query: String, val subs: List<SubCategory> = emptyList())
data class MainCategory(val id: String, val name: String, val mids: List<MidCategory> = emptyList())

object CategoryData {

    val tree: List<MainCategory> = listOf(

        MainCategory("elec","전자기기", listOf(
            MidCategory("tv","TV·영상","TV 모니터", listOf(
                SubCategory("qled","QLED TV","삼성 QLED TV"),
                SubCategory("oled","OLED TV","LG OLED TV"),
                SubCategory("monitor","모니터","컴퓨터 모니터"),
                SubCategory("projector","프로젝터","빔프로젝터"))),
            MidCategory("pc","컴퓨터·노트북","컴퓨터 노트북", listOf(
                SubCategory("laptop","노트북","노트북"),
                SubCategory("desktop","데스크탑","데스크탑 PC"),
                SubCategory("tablet","태블릿","태블릿 PC"),
                SubCategory("keyboard","키보드·마우스","키보드 마우스"),
                SubCategory("ssd","저장장치","SSD HDD 외장하드"))),
            MidCategory("audio","음향기기","이어폰 헤드폰 스피커", listOf(
                SubCategory("wireless_ear","무선이어폰","블루투스 무선이어폰"),
                SubCategory("headphone","헤드폰","노이즈캔슬링 헤드폰"),
                SubCategory("speaker","블루투스스피커","휴대용 블루투스스피커"),
                SubCategory("soundbar","사운드바","TV 사운드바"))),
            MidCategory("mobile","스마트기기","스마트폰 스마트워치", listOf(
                SubCategory("phone","스마트폰","최신 스마트폰"),
                SubCategory("watch","스마트워치","갤럭시워치 애플워치"),
                SubCategory("smarthome","스마트홈","스마트홈 IoT"))),
            MidCategory("appliance","생활가전","생활가전", listOf(
                SubCategory("fridge","냉장고","냉장고"),
                SubCategory("washer","세탁기","드럼세탁기"),
                SubCategory("aircon","에어컨","벽걸이에어컨 스탠드에어컨"),
                SubCategory("cleaner","청소기","무선청소기 로봇청소기"),
                SubCategory("airpur","공기청정기","공기청정기"))),
            MidCategory("camera","카메라","카메라", listOf(
                SubCategory("mirrorless","미러리스","미러리스 카메라"),
                SubCategory("dslr","DSLR","DSLR 카메라"),
                SubCategory("action","액션캠","고프로 액션캠"),
                SubCategory("lens","렌즈","카메라 렌즈"))))),

        MainCategory("fashion","패션", listOf(
            MidCategory("shoes","신발","신발", listOf(
                SubCategory("sneakers","스니커즈","스니커즈 운동화"),
                SubCategory("loafer","구두·로퍼","구두 로퍼"),
                SubCategory("boots","부츠","부츠 앵클"),
                SubCategory("sandal","샌들·슬리퍼","샌들 슬리퍼"))),
            MidCategory("clothes","의류","의류 패션", listOf(
                SubCategory("outer","아우터","패딩 코트 점퍼"),
                SubCategory("top","상의","티셔츠 맨투맨 셔츠"),
                SubCategory("bottom","하의","청바지 슬랙스 반바지"),
                SubCategory("dress","원피스·스커트","원피스 미니스커트"))),
            MidCategory("bag","가방·지갑","가방 지갑", listOf(
                SubCategory("backpack","백팩","백팩"),
                SubCategory("tote","토트백","토트백"),
                SubCategory("cross","크로스백","크로스백 숄더백"),
                SubCategory("wallet","지갑","남성지갑 여성지갑"))),
            MidCategory("acc","패션잡화","패션 액세서리", listOf(
                SubCategory("watch_f","시계","손목시계"),
                SubCategory("sunglass","선글라스","선글라스"),
                SubCategory("cap","모자","볼캡 버킷햇"),
                SubCategory("belt","벨트","남성벨트 여성벨트"))))),

        MainCategory("food","식품", listOf(
            MidCategory("instant","면류·간편식","라면 간편식", listOf(
                SubCategory("ramen","라면","인기 라면"),
                SubCategory("ricebowl","즉석밥","즉석밥 컵밥"),
                SubCategory("frozen","냉동식품","냉동만두 냉동피자"),
                SubCategory("heatup","레토르트","레토르트 가정간편식"))),
            MidCategory("snack","과자·음료","과자 음료", listOf(
                SubCategory("snack_s","과자·스낵","인기 과자"),
                SubCategory("soda","탄산음료","콜라 사이다 탄산음료"),
                SubCategory("coffee","커피·차","원두커피 티백"),
                SubCategory("energy","에너지드링크","에너지드링크"))),
            MidCategory("fresh","신선식품","신선 식품", listOf(
                SubCategory("meat","육류","소고기 돼지고기 닭고기"),
                SubCategory("seafood","수산물","횟감 생선 해산물"),
                SubCategory("veg","채소·과일","유기농 채소 과일"))),
            MidCategory("health_food","건강식품","건강 식품", listOf(
                SubCategory("protein","단백질·보충제","유청단백질 헬스보충제"),
                SubCategory("vitamin","비타민","종합비타민 비타민C"),
                SubCategory("probiotics","유산균","유산균 프로바이오틱스"))))),

        MainCategory("living","생활용품", listOf(
            MidCategory("kitchen","주방","주방용품", listOf(
                SubCategory("cookware","조리도구","프라이팬 냄비"),
                SubCategory("tableware","식기·그릇","그릇 접시 컵"),
                SubCategory("ricecooker","밥솥·가전","전기밥솥 에어프라이어"),
                SubCategory("knife","칼·도마","주방칼 도마"))),
            MidCategory("clean","청소·세탁","청소 세탁", listOf(
                SubCategory("detergent","세제·유연제","세탁세제 섬유유연제"),
                SubCategory("cleantools","청소용품","걸레 청소포 고무장갑"),
                SubCategory("trash","쓰레기봉투·처리","쓰레기봉투"))),
            MidCategory("storage","수납·인테리어","수납 인테리어", listOf(
                SubCategory("shelf","선반·책장","선반 수납장 책장"),
                SubCategory("box","수납함·바구니","수납박스 수납바구니"),
                SubCategory("lighting","조명","LED조명 스탠드조명"))),
            MidCategory("bedding","침구·욕실","침구 욕실", listOf(
                SubCategory("pillow","베개·이불","베개 이불 침대패드"),
                SubCategory("towel","수건·목욕용품","수건 목욕가운"),
                SubCategory("bath","욕실용품","칫솔 치약 샴푸대"))))),

        MainCategory("beauty","뷰티", listOf(
            MidCategory("skin","스킨케어","스킨케어 화장품", listOf(
                SubCategory("serum","에센스·세럼","에센스 세럼 앰플"),
                SubCategory("cream","크림·로션","수분크림 에멀전"),
                SubCategory("mask","마스크팩","마스크팩 시트마스크"),
                SubCategory("sun","선크림·자외선차단","선크림 선스틱"))),
            MidCategory("makeup","메이크업","메이크업 화장품", listOf(
                SubCategory("foundation","파운데이션·쿠션","파운데이션 쿠션"),
                SubCategory("lip","립 메이크업","립스틱 립글로스"),
                SubCategory("eye","아이 메이크업","아이섀도 마스카라"),
                SubCategory("blush","치크·하이라이터","블러셔 하이라이터"))),
            MidCategory("hair","헤어케어","헤어 제품", listOf(
                SubCategory("shampoo","샴푸·컨디셔너","샴푸 트리트먼트"),
                SubCategory("styling","헤어스타일링","헤어왁스 드라이어"),
                SubCategory("colortreat","두피·염색","두피케어 헤어틴트"))),
            MidCategory("body","바디·향수","바디 케어", listOf(
                SubCategory("bodylotion","바디로션·오일","바디로션 바디오일"),
                SubCategory("perfume","향수","여성향수 남성향수"),
                SubCategory("deodorant","데오드란트","데오드란트 방향제"))))),

        MainCategory("sports","스포츠", listOf(
            MidCategory("fitness","헬스·피트니스","헬스 피트니스 운동", listOf(
                SubCategory("weight","웨이트·덤벨","덤벨 바벨 케틀벨"),
                SubCategory("yoga","요가·필라테스","요가매트 필라테스"),
                SubCategory("supple","보충제·영양","단백질 보충제 헬스"),
                SubCategory("sportwear","스포츠의류","레깅스 반바지 러닝화"))),
            MidCategory("outdoor","아웃도어","아웃도어 등산", listOf(
                SubCategory("hiking","등산·트레킹","등산화 등산복"),
                SubCategory("camping","캠핑","텐트 캠핑의자 랜턴"),
                SubCategory("cycling","자전거","자전거 킥보드 헬멧"))),
            MidCategory("ball","구기·라켓","구기 라켓 스포츠", listOf(
                SubCategory("soccer","축구","축구공 축구화"),
                SubCategory("basketball","농구","농구공 농구화"),
                SubCategory("tennis","테니스","테니스라켓 테니스공"),
                SubCategory("badminton","배드민턴","배드민턴라켓 셔틀콕"))),
            MidCategory("water","수상·레저","수상 레저", listOf(
                SubCategory("swim","수영","수영복 수영모 수경"),
                SubCategory("golf","골프","골프채 골프볼 골프의류"))))),

        MainCategory("books","도서", listOf(
            MidCategory("domestic","국내도서","국내 베스트셀러 도서", listOf(
                SubCategory("novel","소설·에세이","소설 에세이 베스트셀러"),
                SubCategory("selfdev","자기계발","자기계발서"),
                SubCategory("economy","경제·경영","경제 경영 투자"),
                SubCategory("history","역사·문화","역사 인문 철학"),
                SubCategory("child","어린이·청소년","어린이책 청소년도서"))),
            MidCategory("expert","전문서적","전문 기술 서적", listOf(
                SubCategory("it","IT·컴퓨터","프로그래밍 IT 개발서적"),
                SubCategory("science","과학·수학","과학 수학 공학"),
                SubCategory("med","의학·건강","의학 건강 운동")))))
    )

    /** 대분류 이름 목록 */
    val mainNames: List<String> get() = tree.map { it.name }

    /** 대분류 ID로 찾기 */
    fun findMain(id: String) = tree.firstOrNull { it.id == id }

    /** 대분류 이름으로 중분류 목록 */
    fun getMids(mainName: String) = tree.firstOrNull { it.name == mainName }?.mids ?: emptyList()

    /** 중분류 이름으로 소분류 목록 */
    fun getSubs(mainName: String, midName: String) =
        getMids(mainName).firstOrNull { it.name == midName }?.subs ?: emptyList()

    /** 소분류까지 포함한 검색 쿼리 반환 */
    fun getQuery(mainName: String, midName: String? = null, subName: String? = null): String {
        val main = tree.firstOrNull { it.name == mainName } ?: return mainName
        if (midName == null) return main.mids.firstOrNull()?.query ?: mainName
        val mid  = main.mids.firstOrNull { it.name == midName } ?: return mainName
        if (subName == null) return mid.query
        return mid.subs.firstOrNull { it.name == subName }?.query ?: mid.query
    }
}
