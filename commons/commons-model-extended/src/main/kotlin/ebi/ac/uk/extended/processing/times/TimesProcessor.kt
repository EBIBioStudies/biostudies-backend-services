package ebi.ac.uk.extended.processing.times

import arrow.core.Try
import ebi.ac.uk.extended.integration.SubmissionService
import ebi.ac.uk.extended.processing.exceptions.InvalidDateFormatException
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.releaseDate
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Calculates the submission release date based on current state of the submission in the system. Calculation rules
 *
 * - If user provides a release date attribute this is used as release time for submission.
 * - If a version of submission already exists it's creation time is used.
 */
class TimesProcessor(private val submissionService: SubmissionService) {

    fun process(submission: Submission): SubmissionDates {
        val now = OffsetDateTime.now()
        val createDate = submissionService.getCreationTime(submission.accNo)
        return SubmissionDates(
            createDate ?: now,
            now,
            submission.releaseDate?.let { parseDate(it) } ?: now)
    }

    private fun parseDate(date: String): OffsetDateTime =
        Try {
            LocalDate.parse(date)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC)
        }.fold({ throw InvalidDateFormatException(date) }, { it })
}

data class SubmissionDates(
    val createTime: OffsetDateTime,
    val modificationTime: OffsetDateTime,
    val releaseDate: OffsetDateTime)
