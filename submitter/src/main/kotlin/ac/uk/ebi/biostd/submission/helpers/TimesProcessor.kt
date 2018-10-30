package ac.uk.ebi.biostd.submission.helpers

import ebi.ac.uk.base.or
import ebi.ac.uk.funtions.now
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.creationTime
import ebi.ac.uk.model.releaseTime
import ebi.ac.uk.model.SubmitOperation
import java.time.OffsetDateTime

class TimesProcessor {

    fun processSubmission(submission: Submission, operation: SubmitOperation): Times {
        return when (operation) {
            SubmitOperation.CREATE ->
                Times(submission.releaseTime.or(now()), now(), now())
            SubmitOperation.UPDATE ->
                Times(submission.releaseTime.or(now()), submission.creationTime, now())
        }
    }
}

class Times(
        private val releaseTime: OffsetDateTime,
        private val creationTime: OffsetDateTime,
        private val modificationTime: OffsetDateTime) {

    operator fun component1(): OffsetDateTime = releaseTime
    operator fun component2(): OffsetDateTime = creationTime
    operator fun component3(): OffsetDateTime = modificationTime
}
