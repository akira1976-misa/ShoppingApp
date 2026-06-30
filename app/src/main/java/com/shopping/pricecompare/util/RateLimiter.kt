package com.shopping.pricecompare.util

import kotlinx.coroutines.delay
import kotlin.math.max

/**
 * /skill-crawler 요구사항: API 제한/차단 회피용 속도 제한(Rate Limiting)
 *
 * 단순 슬라이딩 윈도우 방식.
 * - maxCalls: 윈도우 시간 내 허용 최대 호출 수
 * - windowMs: 윈도우 길이(ms)
 *
 * 네이버 쇼핑 API: 일일 25,000건 제한이 있어 짧은 시간에 과호출하면
 * 429(Too Many Requests) 또는 일시 차단될 수 있으므로,
 * 호출 전 acquire()를 호출해 너무 빠른 연속 호출을 자동으로 지연시킨다.
 */
class RateLimiter(
    private val maxCalls: Int = 10,
    private val windowMs: Long = 1_000L
) {
    private val timestamps = ArrayDeque<Long>()
    private val mutex = kotlinx.coroutines.sync.Mutex()

    suspend fun acquire() {
        mutex.lock()
        try {
            val now = System.currentTimeMillis()
            // 윈도우 밖의 오래된 호출 기록 제거
            while (timestamps.isNotEmpty() && now - timestamps.first() > windowMs) {
                timestamps.removeFirst()
            }
            if (timestamps.size >= maxCalls) {
                val waitTime = max(0L, windowMs - (now - timestamps.first()))
                if (waitTime > 0) delay(waitTime)
            }
            timestamps.addLast(System.currentTimeMillis())
        } finally {
            mutex.unlock()
        }
    }
}
