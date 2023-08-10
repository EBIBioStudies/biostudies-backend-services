package uk.ac.ebi.fire.client.retry

import kotlinx.coroutines.delay
import uk.ac.ebi.fire.client.integration.web.RetryConfig

class SuspendRetryTemplate(
    private val config: RetryConfig,
) {
    suspend fun <T> retry(
        onError: (error: Throwable, attempt: Int) -> Unit,
        block: suspend () -> T
    ): T {
        var currentAttempt = 1
        var currentDelay = config.initialInterval
        repeat(config.maxAttempts - 1) {
            runCatching {
                return block()
            }.onFailure { onError(it, currentAttempt) }

            delay(currentDelay)
            currentDelay = (currentDelay * config.multiplier).toLong().coerceAtMost(config.maxInterval)
            currentAttempt++
        }

        return block()
    }
}
