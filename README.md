# 최저가 쇼핑 앱 (PriceCompare Shopping)

배송비 포함 최저가 비교 쇼핑 Android 앱

## 주요 기능

| 기능 | 설명 |
|------|------|
| 최저가 정렬 | 상품가격 + 배송비 합산 기준 자동 정렬 |
| 카테고리 탐색 | 전자기기, 패션, 식품, 생활용품, 뷰티, 스포츠, 도서 |
| 통합 검색 | 상품명 · 브랜드 · 카테고리 실시간 검색 |
| 다양한 정렬 | 최저가순 / 가격 낮은순 / 가격 높은순 / 리뷰 많은순 / 별점 높은순 |
| 특가 표시 | 🔥 특가 배지 + 할인율 + 정가 표시 |
| 가격 분리 표시 | 상품가격 / 배송비 / **합산금액** 각각 표기 |
| 상품 상세 | 클릭 시 쇼핑몰 페이지로 이동 (WebView + 브라우저 열기) |

## 스크린 구성

```
├── 홈 탭
│   ├── 검색바 (터치 시 검색 화면으로)
│   ├── 카테고리 빠른 선택
│   ├── 오늘의 특가 (가로 스크롤)
│   └── 최저가 상품 (그리드)
│
└── 검색 탭
    ├── 검색바
    └── 카테고리별 탐색
        └── 상품 목록 화면
            ├── 검색 + 카테고리 칩 필터
            ├── 정렬 버튼 (바텀시트)
            └── 상품 그리드 (최저가순)
```

## GitHub Actions 자동 빌드

push 또는 PR 시 자동으로 APK 빌드 → **Actions 탭 → Artifacts**에서 다운로드

## 로컬 빌드 방법

1. Android Studio (Hedgehog 이상) 설치
2. 이 저장소를 Clone
3. Android Studio로 열기
4. `Build → Build Bundle(s)/APK(s) → Build APK(s)`

## GitHub에 올리는 방법

```bash
# 1. 새 저장소 생성 후
git init
git add .
git commit -m "feat: initial shopping app"
git remote add origin https://github.com/[YOUR_USERNAME]/ShoppingApp.git
git push -u origin main
```

> Actions 탭에서 빌드가 자동 시작됩니다. 완료 후 Artifacts에서 APK 다운로드 가능.

## 기술 스택

- **언어**: Kotlin
- **UI**: Material Design 3, ViewBinding
- **네비게이션**: Navigation Component + BottomNavigationView
- **이미지**: Glide
- **아키텍처**: Fragment + ViewModel 기반
