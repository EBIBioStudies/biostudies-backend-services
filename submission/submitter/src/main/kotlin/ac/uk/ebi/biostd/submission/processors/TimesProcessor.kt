package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import arrow.core.Try
import arrow.core.recoverWith
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.addAccessTag
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

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
        submission.releaseTime = submission.releaseDate?.let { parseDate(it) } ?: now

        if (submission.releaseTime.isBefore(now)) {
            submission.released = true
            submission.addAccessTag(SubFields.PUBLIC_ACCESS_TAG.value)
        }
    }

    private fun parseDate(date: String): OffsetDateTime =
        Try {
            LocalDate.parse(date)
        }.recoverWith {
            Try {
                Instant.parse(date).atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }.fold(
            { throw InvalidDateFormatException(date) },
            { it.atStartOfDay().atOffset(ZoneOffset.UTC) }
        )
}
