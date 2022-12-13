package uk.ac.ebi.fire.client.retry

import mu.KotlinLogging
import org.springframework.retry.RetryContext
import org.springframework.retry.support.RetryTemplate
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

/**
 * Wrap function execution function with kotlin @see kotlin.runCatching @see kotlin.time.measureTimedValue to allow
 * descriptive error reporting and measure operation execution time.
 *
 * @param opt the function description.
 * @param func the function to be performed.
 */
@OptIn(ExperimentalTime::class)
internal fun <T> RetryTemplate.execute(opt: String, func: () -> T): T {
    logger.debug(opt) { "Started executing operation: $opt" }
    val response = execute(
        onError = { error, cxt ->
            logger.warn(error) { "Failed to perform operation: $opt. Attempt # ${cxt.retryCount + 1}" }
        },
        func = { measureTimedValue(func) }
    )
    logger.debug { "Finished executing operation: $opt in ${response.duration.inWholeMilliseconds}" }
    return response.value
}

private fun <T> RetryTemplate.execute(onError: (Throwable, RetryContext) -> Unit, func: () -> T): T =
    execute<T, Exception> { runCatching(func).onFailure { error -> onError(error, it) }.getOrThrow() }
