package ac.uk.ebi.biostd.submission.helpers

import ebi.ac.uk.funtions.now
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubmitOperation
import ebi.ac.uk.model.extensions.creationTime
import ebi.ac.uk.model.extensions.releaseTime
import java.time.Instant

class TimesProcessor {

    fun processSubmission(submission: Submission, operation: SubmitOperation): Times {
        return when (operation) {
            SubmitOperation.CREATE ->
                Times(submission.releaseTime ?: now(), now(), now())
            SubmitOperation.UPDATE ->
                Times(submission.releaseTime ?: now(), submission.creationTime, now())
        }
    }
}

class Times(
        private val releaseTime: Instant,
        private val creationTime: Instant,
        private val modificationTime: Instant) {

    operator fun component1(): Instant = releaseTime
    operator fun component2(): Instant = creationTime
    operator fun component3(): Instant = modificationTime
}
