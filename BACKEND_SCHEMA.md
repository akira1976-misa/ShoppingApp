# /skill-api 검토 결과 — 백엔드 DB 스키마 (향후 구현용)

## 현재 상태에 대한 솔직한 안내

`/skill-api`는 풀스택(백엔드+DB+API)을 전제로 하지만,
**지금 이 프로젝트는 백엔드 서버가 없는 순수 Android 앱**입니다.
앱이 네이버/쿠팡 API를 직접 호출하고, 결과를 메모리에서만 가공합니다.

```
지금 구조:
  Android 앱 ──직접 호출──> 네이버 API / 쿠팡 API
  (DB 없음, 서버 없음, 데이터 영구 저장 안 됨)

skill-api가 요구하는 구조:
  Android 앱 ──> 우리 서버(API) ──> DB
                     │
                     └──크롤링──> 네이버/쿠팡/11번가
```

지금 구조의 한계:
- 가격 이력을 저장할 수 없음 (앱을 끄면 사라짐)
- 여러 사용자가 검색한 동일 상품을 캐싱해서 API 호출을 줄일 수 없음
- 가격 알림(목표가 도달 알림) 기능을 만들 수 없음 (서버가 항상 체크해줘야 하는데, 앱은 꺼져있으면 동작 안 함)

**백엔드 없이는 위 3가지를 구현할 수 없습니다.** 지금 당장 백엔드를 만들 필요는 없지만,
나중에 필요해지면 아래 스키마를 기준으로 만들면 됩니다.

---

## DB 스키마 설계 (PostgreSQL 기준)

### 1. categories (카테고리 - 대/중/소분류)

```sql
CREATE TABLE categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    level       SMALLINT NOT NULL,        -- 1=대분류, 2=중분류, 3=소분류
    parent_id   INTEGER REFERENCES categories(id),
    search_query VARCHAR(200),            -- 네이버 API에 보낼 검색어
    sort_order  INTEGER DEFAULT 0,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_categories_parent ON categories(parent_id);
```

### 2. product_groups (동일 상품 그룹 — /skill-match 결과 저장)

```sql
CREATE TABLE product_groups (
    id              SERIAL PRIMARY KEY,
    representative_name VARCHAR(300) NOT NULL,  -- 그룹 대표 상품명
    image_url       VARCHAR(500),
    category_id     INTEGER REFERENCES categories(id),
    brand           VARCHAR(100),
    model_keywords  VARCHAR(200)[],              -- /skill-match가 추출한 키워드(모델명/용량 등)
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_product_groups_category ON product_groups(category_id);
```

### 3. shop_products (쇼핑몰별 실제 상품 — /skill-crawler가 수집)

```sql
CREATE TABLE shop_products (
    id              SERIAL PRIMARY KEY,
    group_id        INTEGER REFERENCES product_groups(id),
    shop_name       VARCHAR(50) NOT NULL,        -- 쿠팡, 네이버쇼핑, 11번가 등
    shop_product_id VARCHAR(100),                -- 쇼핑몰 내 상품 고유 ID
    product_url     VARCHAR(500) NOT NULL,
    raw_title       VARCHAR(300) NOT NULL,        -- 매칭 전 원본 상품명
    is_active       BOOLEAN DEFAULT TRUE,         -- 품절/판매중지 시 FALSE
    last_crawled_at TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_shop_products_group ON shop_products(group_id);
CREATE UNIQUE INDEX idx_shop_products_unique ON shop_products(shop_name, shop_product_id);
```

### 4. price_history (가격 이력 — /skill-crawler가 주기적으로 적재)

```sql
CREATE TABLE price_history (
    id              BIGSERIAL PRIMARY KEY,
    shop_product_id INTEGER REFERENCES shop_products(id),
    price           INTEGER NOT NULL,
    shipping_fee    INTEGER DEFAULT 0,
    total_price     INTEGER NOT NULL,
    recorded_at     TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_price_history_product_time ON price_history(shop_product_id, recorded_at DESC);
```

### 5. price_alerts (가격 알림 — 백엔드가 있어야만 가능한 기능)

```sql
CREATE TABLE price_alerts (
    id           SERIAL PRIMARY KEY,
    user_id      UUID NOT NULL,
    group_id     INTEGER REFERENCES product_groups(id),
    target_price INTEGER NOT NULL,
    is_triggered BOOLEAN DEFAULT FALSE,
    triggered_at TIMESTAMP,
    created_at   TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_price_alerts_user ON price_alerts(user_id);
```

---

## API 엔드포인트 설계 (향후 백엔드 구현 시)

```
GET  /api/categories                          대분류 목록
GET  /api/categories/:id/children             하위 카테고리 목록

GET  /api/products?category_id=:id&page=:n    카테고리별 상품 그룹 목록 (120개씩)
GET  /api/products/search?q=:keyword          키워드 검색

GET  /api/products/:groupId                   상품 그룹 상세 (모든 쇼핑몰 가격 비교)
GET  /api/products/:groupId/price-history      가격 변동 이력

POST /api/alerts                              가격 알림 등록
GET  /api/alerts?user_id=:id                  내 알림 목록
```

---

## 결론

지금 단계(API 키만 발급, 무료 운영)에서는 백엔드 없이 가는 것이 맞습니다.
**가격 알림 기능을 정말 원하시게 되면 그때 위 스키마로 백엔드(Node.js + PostgreSQL)를
별도로 구축**하는 것을 권장합니다. 지금 당장 만들면 서버 호스팅 비용이 매달 발생합니다.
