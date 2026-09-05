package com.shopping.pricecompare.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max

/**
 * /skill-crawler: 슬라이딩 윈도우 방식 Rate Limiter.
 *
 * 버그 수정: 이전 버전은 Mutex를 잠근 채로 delay()를 호출해
 * 다른 코루틴이 영원히 대기하는 데드락 위험이 있었음.
 * 수정: delay 전에 Mutex를 해제하고, 대기 후 다시 획득하도록 변경.
 */
class RateLimiter(
    private val maxCalls: Int = 5,
    private val windowMs: Long = 1_000L
) {
    private val timestamps = ArrayDeque<Long>()
    private val mutex = Mutex()

    suspend fun acquire() {
        while (true) {
            val waitTime = mutex.withLock {
                val now = System.currentTimeMillis()
                // 윈도우 밖 오래된 기록 제거
                while (timestamps.isNotEmpty() && now - timestamps.first() > windowMs) {
                    timestamps.removeFirst()
                }
                if (timestamps.size < maxCalls) {
                    // 여유 있음 → 즉시 기록하고 진행
                    timestamps.addLast(System.currentTimeMillis())
                    0L // 대기 불필요
                } else {
                    // 꽉 참 → 얼마나 기다려야 하는지 계산 후 Mutex 해제
                    max(0L, windowMs - (now - timestamps.first()))
                }
            }
            if (waitTime <= 0L) return   // 진행
            delay(waitTime)              // Mutex 해제된 상태에서 대기 → 데드락 없음
            // 대기 후 루프 재시작 → 다시 timestamps 확인
        }
    }
}
