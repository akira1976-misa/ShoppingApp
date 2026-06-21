/**
 * 딥링크/전환 에이전트 (Deeplink Conversion Agent)
 * ------------------------------------------------
 * 상품 링크를 각 사이트의 "제휴 링크"로 변환합니다.
 * 이게 있어야 사용자가 상품을 클릭했을 때 수익(커미션)이 발생합니다.
 *
 * 주의: 키가 없는 상태에서도 코드 구조를 확인할 수 있도록,
 * 환경변수가 비어있으면 원본 링크를 그대로 반환합니다 (안전한 fallback).
 */

const crypto = require("crypto");

// ===== 쿠팡 파트너스 =====
// 발급처: https://partners.coupang.com -> Tools -> Open API
const COUPANG_ACCESS_KEY = process.env.COUPANG_ACCESS_KEY || "";
const COUPANG_SECRET_KEY = process.env.COUPANG_SECRET_KEY || "";
const COUPANG_DOMAIN = "https://api-gateway.coupang.com";

/**
 * 쿠팡 Open API HMAC 서명 생성
 * 서명은 5분간만 유효하므로 호출 직전에 매번 새로 생성해야 합니다.
 */
function generateCoupangHmac(method, urlPath, secretKey, accessKey) {
  const datetime = new Date()
    .toISOString()
    .replace(/[:-]|\.\d{3}/g, "")
    .slice(0, 15) + "Z"; // 예: 20260620T130000Z

  const message = datetime + method + urlPath;
  const signature = crypto
    .createHmac("sha256", secretKey)
    .update(message)
    .digest("hex");

  return `CEA algorithm=HmacSHA256, access-key=${accessKey}, signed-date=${datetime}, signature=${signature}`;
}

/**
 * 쿠팡 상품을 키워드로 검색하고 제휴 링크가 포함된 결과를 받아옵니다.
 * 주의: 시간당 호출 한도 10회. 반드시 캐싱과 함께 사용하세요.
 */
async function searchCoupangProducts(keyword, limit = 10) {
  if (!COUPANG_ACCESS_KEY || !COUPANG_SECRET_KEY) {
    console.warn("[coupang] API 키가 설정되지 않아 mock 데이터를 반환합니다.");
    return mockCoupangResults(keyword, limit);
  }

  const urlPath = `/v2/providers/affiliate_open_api/apis/openapi/products/search?keyword=${encodeURIComponent(
    keyword
  )}&limit=${limit}`;

  const authorization = generateCoupangHmac(
    "GET",
    urlPath,
    COUPANG_SECRET_KEY,
    COUPANG_ACCESS_KEY
  );

  const response = await fetch(COUPANG_DOMAIN + urlPath, {
    method: "GET",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json;charset=UTF-8",
    },
  });

  if (!response.ok) {
    throw new Error(`쿠팡 API 호출 실패: ${response.status} ${await response.text()}`);
  }

  const data = await response.json();
  // 쿠팡 검색 API 응답에는 이미 제휴 링크(productUrl)가 포함되어 옵니다.
  return (data.data?.productData || []).map((p) => ({
    source: "coupang",
    title: p.productName,
    price: p.productPrice,
    image: p.productImage,
    link: p.productUrl, // 이미 제휴 추적 코드가 포함된 링크
    rating: null,
  }));
}

function mockCoupangResults(keyword, limit) {
  return Array.from({ length: Math.min(limit, 3) }, (_, i) => ({
    source: "coupang",
    title: `${keyword} 쿠팡 예시상품 ${i + 1}`,
    price: 10000 + i * 1500,
    image: "https://via.placeholder.com/200",
    link: `https://www.coupang.com/vp/products/mock-${i}`,
    rating: 4.5,
  }));
}

// ===== 네이버쇼핑 =====
// 발급처: https://developers.naver.com/apps/#/register
const NAVER_CLIENT_ID = process.env.NAVER_CLIENT_ID || "";
const NAVER_CLIENT_SECRET = process.env.NAVER_CLIENT_SECRET || "";

async function searchNaverProducts(keyword, limit = 10) {
  if (!NAVER_CLIENT_ID || !NAVER_CLIENT_SECRET) {
    console.warn("[naver] API 키가 설정되지 않아 mock 데이터를 반환합니다.");
    return mockNaverResults(keyword, limit);
  }

  const url = `https://openapi.naver.com/v1/search/shop.json?query=${encodeURIComponent(
    keyword
  )}&display=${limit}&sort=asc`; // sort=asc: 가격 낮은순

  const response = await fetch(url, {
    headers: {
      "X-Naver-Client-Id": NAVER_CLIENT_ID,
      "X-Naver-Client-Secret": NAVER_CLIENT_SECRET,
    },
  });

  if (!response.ok) {
    throw new Error(`네이버 API 호출 실패: ${response.status} ${await response.text()}`);
  }

  const data = await response.json();
  return (data.items || []).map((item) => ({
    source: "naver",
    title: item.title,
    price: Number(item.lprice),
    image: item.image,
    // !! 중요 !!
    // 네이버쇼핑 "검색 API"(개발자센터에서 받는 일반 오픈API)는
    // 제휴 커미션이 자동으로 붙는 링크가 아닙니다. 단순 검색 결과 제공용입니다.
    // 클릭당/구매당 수익을 받으려면 네이버 "쇼핑 파트너 센터" 또는
    // 별도의 제휴 마케팅 프로그램 가입 절차를 따로 확인해야 합니다.
    // (이 부분은 정책이 바뀔 수 있어 네이버에 직접 문의/확인이 필요합니다.)
    link: item.link,
    mallName: item.mallName,
    rating: null,
  }));
}

function mockNaverResults(keyword, limit) {
  return Array.from({ length: Math.min(limit, 3) }, (_, i) => ({
    source: "naver",
    title: `${keyword} 네이버 예시상품 ${i + 1}`,
    price: 9800 + i * 1200,
    image: "https://via.placeholder.com/200",
    link: `https://shopping.naver.com/mock-${i}`,
    mallName: "스마트스토어 예시",
    rating: 4.7,
  }));
}

module.exports = {
  searchCoupangProducts,
  searchNaverProducts,
  generateCoupangHmac,
};
