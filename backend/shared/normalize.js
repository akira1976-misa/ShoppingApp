/**
 * 상품 정규화 모듈 (Product Normalization Agent)
 * ------------------------------------------------
 * 네이버쇼핑, 쿠팡 등 서로 다른 사이트에서 가져온 상품명을
 * 비교 가능한 형태로 정리하고, 동일 상품인지 판단합니다.
 *
 * 예) "삼성전자 갤럭시 버즈3 SM-R530 화이트 정품"
 *     "Galaxy Buds3 (SM-R530) 화이트"
 *  -> 같은 상품으로 판단
 */

// 비교에 방해되는 불필요한 단어 (마케팅 문구, 배송 관련 등)
const NOISE_WORDS = [
  "정품", "공식", "정식수입", "당일발송", "무료배송", "최저가", "특가",
  "리뷰이벤트", "사은품", "증정", "신상", "NEW", "new", "공식판매처",
  "(주)", "주식회사", "공식스토어", "공식몰", "단독", "한정판",
  "쿠폰", "할인", "세트", "본품", "+", "/"
];

// 자주 쓰이는 브랜드 영문-한글 매핑 (필요한 만큼 계속 추가해서 쓰면 됩니다)
const BRAND_ALIASES = {
  "samsung": "삼성",
  "갤럭시": "galaxy",
  "lg전자": "lg",
  "애플": "apple",
  "다이슨": "dyson",
};

/**
 * HTML 태그, 특수문자, 노이즈 단어를 제거하고 소문자/공백 정리
 */
function cleanTitle(rawTitle) {
  let title = rawTitle
    .replace(/<[^>]*>/g, "")        // HTML 태그 제거 (네이버 API는 <b> 태그가 섞여 옴)
    .replace(/[\[\]【】()（）]/g, " ") // 괄호류 제거
    .toLowerCase()
    .trim();

  for (const noise of NOISE_WORDS) {
    title = title.split(noise.toLowerCase()).join(" ");
  }

  // 브랜드 별칭 통일
  for (const [from, to] of Object.entries(BRAND_ALIASES)) {
    title = title.split(from).join(to);
  }

  // 연속 공백 정리
  title = title.replace(/\s+/g, " ").trim();
  return title;
}

/**
 * 모델명 추출 (영문+숫자 조합, 예: SM-R530, WH-1000XM5)
 * 모델명이 일치하면 같은 상품일 확률이 매우 높습니다.
 */
function extractModelCode(rawTitle) {
  const matches = rawTitle.match(/[A-Za-z]{1,5}-?\d{2,6}[A-Za-z0-9]*/g);
  return matches ? matches.map((m) => m.toUpperCase().replace(/-/g, "")) : [];
}

/**
 * 두 문자열의 유사도를 0~1 사이로 계산 (Jaccard 유사도, 단어 집합 기준)
 * 외부 라이브러리 없이 가볍게 동작하도록 구현
 */
function similarity(titleA, titleB) {
  const setA = new Set(cleanTitle(titleA).split(" ").filter((w) => w.length > 1));
  const setB = new Set(cleanTitle(titleB).split(" ").filter((w) => w.length > 1));
  if (setA.size === 0 || setB.size === 0) return 0;

  let intersection = 0;
  for (const word of setA) {
    if (setB.has(word)) intersection++;
  }
  const union = setA.size + setB.size - intersection;
  return intersection / union;
}

/**
 * 상품 매칭 핵심 함수
 * @param {string} titleA
 * @param {string} titleB
 * @returns {{isMatch: boolean, score: number, reason: string}}
 */
function isSameProduct(titleA, titleB) {
  const modelsA = extractModelCode(titleA);
  const modelsB = extractModelCode(titleB);

  // 모델 코드가 양쪽에 다 있고 하나라도 일치하면 강한 신호로 같은 상품 판정
  const modelMatch = modelsA.some((m) => modelsB.includes(m));
  if (modelMatch) {
    return { isMatch: true, score: 0.95, reason: "model_code_match" };
  }

  const score = similarity(titleA, titleB);
  // 임계값 0.45는 실제 데이터로 튜닝이 필요한 값입니다. 처음엔 이 값으로 시작하세요.
  return {
    isMatch: score >= 0.45,
    score,
    reason: score >= 0.45 ? "title_similarity" : "no_match",
  };
}

/**
 * 여러 사이트에서 모인 상품 리스트를 그룹핑합니다.
 * 입력: [{ source: 'naver', title, price, link, image }, ...]
 * 출력: [{ canonicalTitle, items: [...], lowestPrice, lowestSource }, ...]
 */
function groupProductsAcrossSources(items) {
  const groups = [];

  for (const item of items) {
    let matchedGroup = null;

    for (const group of groups) {
      const { isMatch } = isSameProduct(group.canonicalTitle, item.title);
      if (isMatch) {
        matchedGroup = group;
        break;
      }
    }

    if (matchedGroup) {
      matchedGroup.items.push(item);
    } else {
      groups.push({
        canonicalTitle: item.title,
        items: [item],
      });
    }
  }

  // 그룹별 최저가 계산
  for (const group of groups) {
    const sorted = [...group.items].sort((a, b) => a.price - b.price);
    group.lowestPrice = sorted[0].price;
    group.lowestSource = sorted[0].source;
    group.lowestLink = sorted[0].link;
    group.priceRange = {
      min: sorted[0].price,
      max: sorted[sorted.length - 1].price,
    };
  }

  return groups;
}

module.exports = {
  cleanTitle,
  extractModelCode,
  similarity,
  isSameProduct,
  groupProductsAcrossSources,
};
