package com.shopping.pricecompare.util

import kotlinx.coroutines.delay

/**
 * /skill-crawler 요구사항: API 차단/오류 회피용 에러 핸들링
 *
 * 지수 백오프(Exponential Backoff) 방식 재시도.
 * 일시적 네트워크 오류, 429(Rate Limit), 5xx 서버 오류 시
 * 점점 늘어나는 대기시간을 두고 재시도한다.
 * 영구적 오류(401 인증 실패 등)는 재시도하지 않고 즉시 실패 처리한다.
 */
object RetryPolicy {

    suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 300L,
        shouldRetry: (Throwable) -> Boolean = { true },
        block: suspend (attempt: Int) -> T
    ): T {
        var lastError: Throwable? = null
        repeat(maxAttempts) { attempt ->
            try {
                return block(attempt)
            } catch (e: Throwable) {
                lastError = e
                if (attempt == maxAttempts - 1 || !shouldRetry(e)) throw e
                val backoff = initialDelayMs * (1 shl attempt) // 300ms, 600ms, 1200ms...
                delay(backoff)
            }
        }
        throw lastError ?: IllegalStateException("재시도 실패")
    }
}
