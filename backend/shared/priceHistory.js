/**
 * 가격 히스토리 추적 에이전트 (Price History Agent)
 * ------------------------------------------------
 * 매번 수집된 최저가를 누적 저장해서,
 * "이 상품이 최근 30일간 어떻게 변했는지" 보여줄 수 있게 합니다.
 *
 * 저장 방식: 일단 JSON 파일 기반 (GitHub Actions 환경에 적합)
 * 사용자가 늘어나면 추후 Cloudflare KV, Supabase 등으로 교체 가능하도록
 * 함수 인터페이스만 그대로 유지하면 됩니다.
 */

const fs = require("fs");
const path = require("path");

const HISTORY_DIR = path.join(__dirname, "..", "data", "price-history");

function ensureDir() {
  if (!fs.existsSync(HISTORY_DIR)) {
    fs.mkdirSync(HISTORY_DIR, { recursive: true });
  }
}

/**
 * 상품 식별을 위한 안전한 파일명 생성 (간단한 해시)
 */
function productKey(canonicalTitle) {
  return canonicalTitle
    .toLowerCase()
    .replace(/[^a-z0-9가-힣]/g, "_")
    .slice(0, 80);
}

/**
 * 오늘자 최저가 기록을 추가합니다.
 * @param {string} canonicalTitle 정규화된 상품명 (normalize.js의 groupProductsAcrossSources 결과 사용)
 * @param {number} lowestPrice
 * @param {string} lowestSource
 */
function recordPrice(canonicalTitle, lowestPrice, lowestSource) {
  ensureDir();
  const key = productKey(canonicalTitle);
  const filePath = path.join(HISTORY_DIR, `${key}.json`);

  let history = [];
  if (fs.existsSync(filePath)) {
    history = JSON.parse(fs.readFileSync(filePath, "utf-8"));
  }

  history.push({
    date: new Date().toISOString(),
    price: lowestPrice,
    source: lowestSource,
  });

  // 너무 오래된 기록은 정리 (최근 90일만 유지)
  const ninetyDaysAgo = Date.now() - 90 * 24 * 60 * 60 * 1000;
  history = history.filter((h) => new Date(h.date).getTime() >= ninetyDaysAgo);

  fs.writeFileSync(filePath, JSON.stringify(history, null, 2));
  return history;
}

/**
 * 상품의 가격 히스토리를 가져옵니다.
 */
function getHistory(canonicalTitle) {
  const key = productKey(canonicalTitle);
  const filePath = path.join(HISTORY_DIR, `${key}.json`);
  if (!fs.existsSync(filePath)) return [];
  return JSON.parse(fs.readFileSync(filePath, "utf-8"));
}

/**
 * "지금이 살 때인지" 간단 판단
 * 최근 90일 평균과 현재가를 비교
 */
function evaluateDealQuality(canonicalTitle, currentPrice) {
  const history = getHistory(canonicalTitle);
  if (history.length < 3) {
    return { verdict: "데이터 부족", averagePrice: null };
  }

  const avg = history.reduce((sum, h) => sum + h.price, 0) / history.length;
  const diffPercent = ((currentPrice - avg) / avg) * 100;

  let verdict = "평균 수준";
  if (diffPercent <= -10) verdict = "최근 평균보다 많이 저렴함 (구매 추천)";
  else if (diffPercent >= 10) verdict = "최근 평균보다 비쌈 (대기 추천)";

  return {
    verdict,
    averagePrice: Math.round(avg),
    diffPercent: Math.round(diffPercent * 10) / 10,
  };
}

module.exports = {
  recordPrice,
  getHistory,
  evaluateDealQuality,
  productKey,
};
