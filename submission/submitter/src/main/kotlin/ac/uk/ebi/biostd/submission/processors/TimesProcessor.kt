package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

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

        submission.modificationTime = now
        submission.creationTime = createDate ?: now
        submission.releaseTime = getReleaseDate(submission)
    }

    private fun getReleaseDate(submission: ExtendedSubmission): OffsetDateTime {
        var releaseDate = OffsetDateTime.now()
        submission.releaseDate?.let {
            try {
                releaseDate = LocalDate.parse(it).atStartOfDay().atOffset(ZoneOffset.UTC)
            } catch (exception: DateTimeParseException) {
                throw InvalidDateFormatException(it)
            }
        }

        return releaseDate
    }
}
