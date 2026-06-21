/**
 * 카테고리 주기 수집 에이전트 (Collector Agent)
 * ------------------------------------------------
 * GitHub Actions가 1~3시간마다 이 스크립트를 실행합니다.
 * 미리 정의된 카테고리/인기 검색어들의 최저가를 모아서
 * /data/category-cache/*.json 으로 저장합니다.
 *
 * 앱은 이 JSON 파일을 GitHub Raw URL로 그냥 읨어가면 됩니다.
 * 예: https://raw.githubusercontent.com/USERNAME/REPO/main/data/category-cache/electronics.json
 */

const fs = require("fs");
const path = require("path");
const { searchCoupangProducts, searchNaverProducts } = require("../shared/deeplink");
const { groupProductsAcrossSources } = require("../shared/normalize");
const { recordPrice } = require("../shared/priceHistory");

// 카테고리별로 수집할 대표 검색어들.
// 처음엔 적게 시작하고, 쿠팡 API 시간당 10회 제한을 꼭 감안해서 늘려가세요.
const CATEGORIES = {
  electronics: ["무선이어폰", "보조배터리", "블루투스스피커"],
  fashion: ["여름반팔티", "운동화"],
  home: ["휴대용선풍기", "텀블러"],
};

const OUTPUT_DIR = path.join(__dirname, "..", "data", "category-cache");

function ensureDir() {
  if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
  }
}

/**
 * 검색어 하나에 대해 양쪽 사이트를 호출하고, 정규화 + 그룹핑까지 처리
 */
async function collectKeyword(keyword) {
  const [coupangResults, naverResults] = await Promise.all([
    searchCoupangProducts(keyword, 10).catch((err) => {
      console.error(`[coupang] "${keyword}" 수집 실패:`, err.message);
      return [];
    }),
    searchNaverProducts(keyword, 10).catch((err) => {
      console.error(`[naver] "${keyword}" 수집 실패:`, err.message);
      return [];
    }),
  ]);

  const allItems = [...coupangResults, ...naverResults];
  const groups = groupProductsAcrossSources(allItems);

  // 각 그룹의 최저가를 히스토리에 기록 (가격 변동 추적용)
  for (const group of groups) {
    recordPrice(group.canonicalTitle, group.lowestPrice, group.lowestSource);
  }

  return groups;
}

async function collectCategory(categoryName, keywords) {
  console.log(`[수집 시작] 카테고리: ${categoryName}`);
  const results = {};

  for (const keyword of keywords) {
    // 쿠팡 API 시간당 10회 제한이 있으니 호출 사이에 약간의 간격을 둡니다.
    results[keyword] = await collectKeyword(keyword);
    await sleep(2000);
  }

  ensureDir();
  const outputPath = path.join(OUTPUT_DIR, `${categoryName}.json`);
  fs.writeFileSync(
    outputPath,
    JSON.stringify(
      {
        category: categoryName,
        updatedAt: new Date().toISOString(),
        keywords: results,
      },
      null,
      2
    )
  );
  console.log(`[저장 완료] ${outputPath}`);
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function main() {
  for (const [categoryName, keywords] of Object.entries(CATEGORIES)) {
    await collectCategory(categoryName, keywords);
  }
  console.log("모든 카테고리 수집 완료.");
}

main().catch((err) => {
  console.error("수집 중 치명적 오류:", err);
  process.exit(1);
});
