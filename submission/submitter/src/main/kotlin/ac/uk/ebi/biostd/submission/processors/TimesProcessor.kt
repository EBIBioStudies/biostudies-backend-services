package ac.uk.ebi.biostd.submission.processors

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

/**
 * Calculates the submission release date based on current state of the submission in the system. Calculation rules
 *
 * - If user provides a release date attribute this is used as release time for submission.
 * - If a version of submission already exists it's creation time is used.
 */
class TimesProcessor : SubmissionProcessor {

    override fun process(submission: ExtendedSubmission, context: PersistenceContext) {
        val now = OffsetDateTime.now()
        val createDate = context.getSubmission(submission.accNo)?.creationTime
        val releaseDate = submission.releaseDate?.atStartOfDay()?.atOffset(UTC)

        submission.modificationTime = now
        submission.releaseTime = releaseDate ?: now
        submission.creationTime = createDate ?: now
    }
}
