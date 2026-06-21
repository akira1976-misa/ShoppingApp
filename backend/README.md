# PriceCompare 백엔드 (backend/)

쇼핑 최저가 비교 앱(최저가 쇼핑)의 백엔드 없는 데이터 수집/검색 구조입니다.
서버를 직접 운영하지 않고도 동작하도록 설계했습니다.
이 폴더는 `ShoppingApp` 저장소 안의 `backend/` 위치에 있는 것을 전제로 합니다.

## 폴더 구조

```
backend/
├── shared/
│   ├── normalize.js      # 상품 정규화 (다른 사이트의 같은 상품 매칭)
│   ├── deeplink.js       # 쿠팡/네이버 API 호출 + 딥링크 생성 (Node.js용)
│   └── priceHistory.js   # 가격 변동 기록 및 "지금 살때인지" 판단
├── collector/
│   └── collect.js        # 카테고리별 주기 수집 메인 스크립트
├── search-function/
│   ├── src/index.js      # 실시간 검색 (Cloudflare Workers용)
│   └── wrangler.toml     # Workers 배포 설정
└── data/                  # 수집 결과가 저장되는 곳 (자동 생성, git에 커밋됨)
    ├── category-cache/    # 카테고리별 최저가 캐시 (앱이 읍어가는 파일)
    └── price-history/     # 상품별 가격 변동 히스토리
```

GitHub Actions 워크플로우는 저장소 최상위 `.github/workflows/collect-prices.yml`에 있습니다
(이 폴더 바깥, `ShoppingApp/.github/workflows/`).

## 지금 키 없이도 되는 것

모든 모듈이 API 키가 없으면 **자동으로 mock(가상) 데이터**를 반환합니다.

```bash
cd backend
node collector/collect.js   # mock 데이터로 전체 파이프라인 테스트
```

## 키가 발급되면 해야 할 일

### 1) GitHub 저장소 Settings → Secrets and variables → Actions 에서 등록
- `COUPANG_ACCESS_KEY`
- `COUPANG_SECRET_KEY`
- `NAVER_CLIENT_ID`
- `NAVER_CLIENT_SECRET`

등록만 하면 `.github/workflows/collect-prices.yml`이 자동으로 실제 API를 호출합니다.

### 2) Cloudflare Workers 배포 (실시간 검색용)
```bash
cd backend/search-function
npm install -g wrangler   # 최초 1회
wrangler login
wrangler secret put COUPANG_ACCESS_KEY
wrangler secret put COUPANG_SECRET_KEY
wrangler secret put NAVER_CLIENT_ID
wrangler secret put NAVER_CLIENT_SECRET
wrangler deploy
```

## 안드로이드 앱에서 호출하는 방법

### (A) 카테고리 화면 — GitHub Actions가 만든 캐시 파일을 그냥 읍기
```kotlin
val url = "https://raw.githubusercontent.com/사용자명/ShoppingApp/main/backend/data/category-cache/electronics.json"
```

### (B) 검색 화면 — Cloudflare Workers 실시간 호출
```kotlin
val url = "https://pricecompare-search.사용자계정.workers.dev/search?q=" + 검색어
```

## 주의해야 할 제약사항

| 항목 | 내용 |
|---|---|
| 쿠팡 검색 API 호출 한도 | 시간당 최대 10회 (계정당) — 캐싱이 필수입니다 |
| 쿠팡 HMAC 서명 유효시간 | 5분 — 코드에서 매 호출마다 새로 생성하도록 이미 구현됨 |
| 네이버 제휴 수익 | 네이버쇼핑 "검색 API"는 제휴 커미션이 자동으로 붙지 않습니다. 수익화를 원하면 네이버 쇼핑 파트너 센터에 별도로 문의가 필요합니다 |
| 11번가 | 현재 신규 오픈 API 발급이 제한적입니다. 일단 네이버+쿠팡으로 시작하는 걸 추천합니다 |
| 상품 매칭 정확도 | `normalize.js`의 유사도 임계값(0.45)은 시작값입니다. 실제 데이터를 보면서 조정이 필요할 수 있습니다 |

## 다음에 추가하면 좋은 것

- 가격 알림 (FCM 푸시) — `priceHistory.js`의 `evaluateDealQuality`를 활용
- 리뷰/평점 통합
