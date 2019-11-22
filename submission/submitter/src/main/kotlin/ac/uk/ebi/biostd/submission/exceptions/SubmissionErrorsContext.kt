package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.util.collections.ifNotEmpty
import mu.KotlinLogging

internal class SubmissionErrorsContext {
    private val logger = KotlinLogging.logger {}
    private val exceptionList = mutableListOf<Throwable>()

    fun runCatching(function: () -> Unit) {
        Result.runCatching { function() }.onFailure { registerError(it) }
    }

    fun handleErrors() = exceptionList.ifNotEmpty {
        throw InvalidSubmissionException("Submission validation errors", exceptionList)
    }

    private fun registerError(error: Throwable) {
        exceptionList.add(error)
        logger.error { error.stackTrace }
    }
}
