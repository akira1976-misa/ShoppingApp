# ShoppingApp 저장소에 백엔드 추가하는 방법

이 zip 안의 내용을 기존 `ShoppingApp` 폴더(로컬 디스크 C: > 앱 개발 > ShoppingApp_deeplink2 > ShoppingApp)에
그대로 덮어씌우면(병합하면) 됩니다.

## 적용 후 최종 구조

```
ShoppingApp/                       ← 기존 그대로
├── app/                           ← 기존 안드로이드 앱 (그대로 유지, 손대지 않음)
├── gradle/
├── build.gradle
├── settings.gradle
├── .gitignore
├── gradle.properties
├── local.properties
│
├── backend/                       ← ★ 새로 추가되는 폴더
│   ├── shared/
│   │   ├── normalize.js
│   │   ├── deeplink.js
│   │   └── priceHistory.js
│   ├── collector/
│   │   └── collect.js
│   ├── search-function/
│   │   ├── src/index.js
│   │   └── wrangler.toml
│   └── package.json
│
└── .github/
    └── workflows/
        ├── (기존에 있던 안드로이드 빌드 워크플로우 — 그대로 유지)
        └── collect-prices.yml    ← ★ 새로 추가되는 파일
스킬 명령 상세 (System Prompt):

"당신은 Node.js(또는 사용 중인 백엔드 언어) 기반의 웹 크롤링 및 API 연동 전문가입니다. 네이버쇼핑 API, 쿠팡 파트너스 API, 11번가 오픈 API를 연동하여 특정 키워드의 상품명, 실시간 가격, 상품 상세 링크, 썸네일 이미지를 JSON 형태로 가져오는 백엔드 서비스 코드를 작성하세요. API 제한이나 차단을 회피하기 위한 에러 핸들링과 속도 제한(Rate Limiting) 로직을 반드시 포함해야 합니다."
