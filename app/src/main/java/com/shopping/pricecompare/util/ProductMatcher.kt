package com.shopping.pricecompare.util

import kotlin.math.max
import kotlin.math.min

/**
 * /skill-match: 동일 상품 판별 및 그룹화 알고리즘.
 *
 * 서로 다른 쇼핑몰(네이버, 쿠팡 등)에서 수집한 상품명을 분석하여
 * 같은 실제 상품인지 판별하고 그룹 ID를 부여한다.
 *
 * 사용 기술:
 *  - 키워드 추출(모델명/용량/색상 등 핵심 토큰 분리)
 *  - Jaro-Winkler 유사도 (짧은 문자열, 어순 변화에 강함)
 *  - Levenshtein Distance 보조 검증
 */
object ProductMatcher {

    /** 매칭 판정 임계값 (0.0~1.0). 이 값 이상이면 동일 상품으로 간주 */
    private const val SIMILARITY_THRESHOLD = 0.82

    /**
     * 두 상품명이 동일 상품인지 판별
     */
    fun isSameProduct(nameA: String, nameB: String): Boolean {
        val keywordsA = extractKeywords(nameA)
        val keywordsB = extractKeywords(nameB)

        // 1차: 핵심 키워드(모델명/용량 등) 교집합이 충분하면 강한 신호로 간주
        val keywordOverlap = keywordOverlapRatio(keywordsA, keywordsB)
        if (keywordOverlap >= 0.6) return true

        // 2차: 정규화된 전체 문자열의 Jaro-Winkler 유사도
        val normA = normalize(nameA)
        val normB = normalize(nameB)
        val similarity = jaroWinkler(normA, normB)
        return similarity >= SIMILARITY_THRESHOLD
    }

    /**
     * 상품 목록을 동일 상품 그룹으로 클러스터링한다.
     * 반환값: 그룹 ID -> 같은 그룹에 속한 원본 인덱스 목록
     */
    fun cluster(names: List<String>): Map<Int, List<Int>> {
        val groupOf = IntArray(names.size) { -1 }
        var nextGroupId = 0

        for (i in names.indices) {
            if (groupOf[i] != -1) continue
            groupOf[i] = nextGroupId
            for (j in i + 1 until names.size) {
                if (groupOf[j] != -1) continue
                if (isSameProduct(names[i], names[j])) {
                    groupOf[j] = nextGroupId
                }
            }
            nextGroupId++
        }

        return groupOf.indices.groupBy { groupOf[it] }
    }

    // -----------------------------------------------------------------
    //  키워드 추출 (모델명, 용량, 색상 등)
    // -----------------------------------------------------------------

    private val CAPACITY_REGEX = Regex("(\\d+)\\s*(GB|TB|MB|ML|L|KG|G)", RegexOption.IGNORE_CASE)
    private val MODEL_REGEX = Regex("[A-Z]{1,4}-?\\d{2,6}[A-Z]?") // 예: SM-S928N, S24, RTX4090
    private val COLOR_KEYWORDS = listOf(
        "블랙","화이트","실버","골드","핑크","블루","그레이","레드","그린","퍼플",
        "black","white","silver","gold","pink","blue","gray","red","green","purple"
    )

    /** 상품명에서 모델명/용량/색상 등 핵심 키워드 토큰 집합 추출 */
    fun extractKeywords(name: String): Set<String> {
        val upper = name.uppercase()
        val keywords = mutableSetOf<String>()

        CAPACITY_REGEX.findAll(upper).forEach { keywords.add(it.value.replace(" ", "")) }
        MODEL_REGEX.findAll(upper).forEach { keywords.add(it.value) }
        COLOR_KEYWORDS.forEach { color ->
            if (upper.contains(color.uppercase())) keywords.add(color.uppercase())
        }
        return keywords
    }

    private fun keywordOverlapRatio(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val intersection = a.intersect(b).size
        val union = a.union(b).size
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }

    /** 브랜드/불용어/특수문자 제거 등 비교 전 정규화 */
    private fun normalize(name: String): String {
        return name
            .lowercase()
            .replace(Regex("[\\[\\](){}]"), " ")
            .replace(Regex("[^a-z0-9가-힣 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // -----------------------------------------------------------------
    //  Jaro-Winkler 유사도
    // -----------------------------------------------------------------

    fun jaroWinkler(s1: String, s2: String): Double {
        val jaro = jaro(s1, s2)
        val prefixLen = commonPrefixLength(s1, s2, 4)
        val scalingFactor = 0.1
        return jaro + (prefixLen * scalingFactor * (1 - jaro))
    }

    private fun jaro(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val matchDistance = max(s1.length, s2.length) / 2 - 1
        val s1Matches = BooleanArray(s1.length)
        val s2Matches = BooleanArray(s2.length)
        var matches = 0

        for (i in s1.indices) {
            val start = max(0, i - matchDistance)
            val end = min(i + matchDistance + 1, s2.length)
            for (j in start until end) {
                if (s2Matches[j]) continue
                if (s1[i] != s2[j]) continue
                s1Matches[i] = true
                s2Matches[j] = true
                matches++
                break
            }
        }
        if (matches == 0) return 0.0

        var k = 0
        var transpositions = 0
        for (i in s1.indices) {
            if (!s1Matches[i]) continue
            while (!s2Matches[k]) k++
            if (s1[i] != s2[k]) transpositions++
            k++
        }
        val t = transpositions / 2.0
        val m = matches.toDouble()
        return (m / s1.length + m / s2.length + (m - t) / m) / 3.0
    }

    private fun commonPrefixLength(s1: String, s2: String, maxLen: Int): Int {
        val limit = min(maxLen, min(s1.length, s2.length))
        var i = 0
        while (i < limit && s1[i] == s2[i]) i++
        return i
    }

    // -----------------------------------------------------------------
    //  Levenshtein Distance (보조 검증용)
    // -----------------------------------------------------------------

    fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) dp[i - 1][j - 1]
                else 1 + min(dp[i - 1][j], min(dp[i][j - 1], dp[i - 1][j - 1]))
            }
        }
        return dp[s1.length][s2.length]
    }

    /** Levenshtein 기반 정규화 유사도 (0~1, 1이 완전 일치) */
    fun levenshteinSimilarity(s1: String, s2: String): Double {
        val maxLen = max(s1.length, s2.length)
        if (maxLen == 0) return 1.0
        return 1.0 - (levenshtein(s1, s2).toDouble() / maxLen)
    }
}
