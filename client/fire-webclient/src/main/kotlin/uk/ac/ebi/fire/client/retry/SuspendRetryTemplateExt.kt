import mu.KotlinLogging
import uk.ac.ebi.fire.client.retry.SuspendRetryTemplate

private val logger = KotlinLogging.logger {}

internal suspend fun <T> SuspendRetryTemplate.execute(opt: String, func: suspend () -> T): T {
    logger.debug(opt) { "Started executing operation: $opt" }

    val response = retry(
        onError = { error, attempt ->
            val errorMsg = error.cause?.message ?: error.message
            logger.warn(error) { "Failed to perform operation: $opt on attempt # $attempt with error: $errorMsg" }
        },
        func,
    )

    logger.debug { "Finished executing operation: $opt" }

    return response
}
