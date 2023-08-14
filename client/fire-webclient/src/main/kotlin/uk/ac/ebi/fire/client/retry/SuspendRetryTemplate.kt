package uk.ac.ebi.fire.client.retry

import kotlinx.coroutines.delay
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.RetryConfig

private val logger = KotlinLogging.logger {}

class SuspendRetryTemplate(
    private val config: RetryConfig,
) {
    suspend fun <T> execute(opt: String, func: suspend () -> T): T {
        logger.debug(opt) { "Started executing operation: $opt" }

        val response = retry(opt, func)

        logger.debug { "Finished executing operation: $opt" }

        return response
    }

    private suspend fun <T> retry(opt: String, func: suspend () -> T): T {
        var attempt = 1
        var currentDelay = config.initialInterval

        repeat(config.maxAttempts - 1) {
            runCatching {
                return func()
            }.onFailure {
                val errorMsg = it.cause?.message ?: it.message
                logger.warn(it) { "Failed to perform operation: $opt on attempt # $attempt with error: $errorMsg" }
            }

            delay(currentDelay)
            currentDelay = (currentDelay * config.multiplier).toLong().coerceAtMost(config.maxInterval)
            attempt++
        }

        return func()
    }
}
