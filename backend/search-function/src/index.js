/**
 * 실시간 검색 에이전트 (Search Function)
 * ------------------------------------------------
 * Cloudflare Workers에서 동작하는 서버리스 함수.
 * 앱에서 사용자가 검색어를 입력하면 이 엔드포인트를 호출합니다.
 *
 * 장점: 별도 서버 운영 없이, 요청이 올 때만 코드가 실행됩니다 (무료 티어로 충분히 시작 가능).
 *
 * 배포 방법 (나중에):
 *   1) npm install -g wrangler
 *   2) wrangler login
 *   3) wrangler secret put COUPANG_ACCESS_KEY  (등 키들 등록)
 *   4) wrangler deploy
 *
 * 호출 예: https://your-worker.workers.dev/search?q=무선이어폰
 */

// ===== 상품 정규화 로직 (normalize.js와 동일 내용을 Workers 환경에 맞게 인라인화) =====
function cleanTitle(rawTitle) {
  return rawTitle
    .replace(/<[^>]*>/g, "")
    .replace(/[\[\]【】()（）]/g, " ")
    .toLowerCase()
    .replace(/\s+/g, " ")
    .trim();
}

function similarity(titleA, titleB) {
  const setA = new Set(cleanTitle(titleA).split(" ").filter((w) => w.length > 1));
  const setB = new Set(cleanTitle(titleB).split(" ").filter((w) => w.length > 1));
  if (setA.size === 0 || setB.size === 0) return 0;
  let intersection = 0;
  for (const word of setA) if (setB.has(word)) intersection++;
  return intersection / (setA.size + setB.size - intersection);
}

function groupProducts(items) {
  const groups = [];
  for (const item of items) {
    const matched = groups.find((g) => similarity(g.canonicalTitle, item.title) >= 0.45);
    if (matched) {
      matched.items.push(item);
    } else {
      groups.push({ canonicalTitle: item.title, items: [item] });
    }
  }
  for (const group of groups) {
    const sorted = [...group.items].sort((a, b) => a.price - b.price);
    group.lowestPrice = sorted[0].price;
    group.lowestSource = sorted[0].source;
    group.lowestLink = sorted[0].link;
  }
  return groups;
}

// ===== 쿠팡 HMAC 인증 (Workers 환경은 Web Crypto API 사용) =====
async function generateCoupangHmac(method, urlPath, secretKey, accessKey) {
  const datetime =
    new Date()
      .toISOString()
      .replace(/[:-]|\.\d{3}/g, "")
      .slice(0, 15) + "Z";

  const message = datetime + method + urlPath;
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(secretKey),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const signatureBuffer = await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(message));
  const signature = Array.from(new Uint8Array(signatureBuffer))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");

  return `CEA algorithm=HmacSHA256, access-key=${accessKey}, signed-date=${datetime}, signature=${signature}`;
}

async function searchCoupang(keyword, env) {
  if (!env.COUPANG_ACCESS_KEY || !env.COUPANG_SECRET_KEY) {
    return []; // 키 없으면 조용히 빈 배열 (네이버만으로도 결과는 보여줄 수 있게)
  }
  const urlPath = `/v2/providers/affiliate_open_api/apis/openapi/products/search?keyword=${encodeURIComponent(
    keyword
  )}&limit=10`;
  const authorization = await generateCoupangHmac("GET", urlPath, env.COUPANG_SECRET_KEY, env.COUPANG_ACCESS_KEY);

  const response = await fetch("https://api-gateway.coupang.com" + urlPath, {
    headers: { Authorization: authorization, "Content-Type": "application/json;charset=UTF-8" },
  });
  if (!response.ok) return [];
  const data = await response.json();
  return (data.data?.productData || []).map((p) => ({
    source: "coupang",
    title: p.productName,
    price: p.productPrice,
    image: p.productImage,
    link: p.productUrl,
  }));
}

async function searchNaver(keyword, env) {
  if (!env.NAVER_CLIENT_ID || !env.NAVER_CLIENT_SECRET) {
    return [];
  }
  const url = `https://openapi.naver.com/v1/search/shop.json?query=${encodeURIComponent(keyword)}&display=10&sort=asc`;
  const response = await fetch(url, {
    headers: {
      "X-Naver-Client-Id": env.NAVER_CLIENT_ID,
      "X-Naver-Client-Secret": env.NAVER_CLIENT_SECRET,
    },
  });
  if (!response.ok) return [];
  const data = await response.json();
  return (data.items || []).map((item) => ({
    source: "naver",
    title: item.title,
    price: Number(item.lprice),
    image: item.image,
    link: item.link,
    mallName: item.mallName,
  }));
}

// ===== Workers 진입점 =====
export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    // CORS 처리 (앱에서 직접 호출하므로 필요)
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type",
        },
      });
    }

    if (url.pathname !== "/search") {
      return new Response("Not found", { status: 404 });
    }

    const keyword = url.searchParams.get("q");
    if (!keyword || keyword.trim().length === 0) {
      return jsonResponse({ error: "검색어(q)를 입력해주세요." }, 400);
    }

    try {
      const [coupangResults, naverResults] = await Promise.all([
        searchCoupang(keyword, env),
        searchNaver(keyword, env),
      ]);

      const grouped = groupProducts([...coupangResults, ...naverResults]);

      // 최저가 순으로 정렬해서 반환
      grouped.sort((a, b) => a.lowestPrice - b.lowestPrice);

      return jsonResponse({
        keyword,
        resultCount: grouped.length,
        results: grouped,
      });
    } catch (err) {
      return jsonResponse({ error: "검색 중 오류가 발생했습니다.", detail: err.message }, 500);
    }
  },
};

function jsonResponse(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Access-Control-Allow-Origin": "*",
    },
  });
}
