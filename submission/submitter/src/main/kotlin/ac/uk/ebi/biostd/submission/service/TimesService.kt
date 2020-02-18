package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Calculates the submission release date based on current state of the submission in the system. Calculation rules.
 */
class TimesService(private val context: PersistenceContext) {
    internal fun getTimes(submission: ExtendedSubmission): Times {
        val now = OffsetDateTime.now()
        val creationTime = context.getSubmission(submission.accNo)?.creationTime ?: now
        val releaseTime = submission.releaseDate?.let { parseDate(it) }
        return Times(creationTime, now, releaseTime)
    }

    private fun parseDate(date: String): OffsetDateTime =
        runCatching { LocalDate.parse(date) }
            .recoverCatching { Instant.parse(date).atZone(ZoneId.systemDefault()).toLocalDate() }
            .fold({ it.atStartOfDay().atOffset(ZoneOffset.UTC) }, { throw InvalidDateFormatException(date) })
}

data class Times(
    val createTime: OffsetDateTime,
    val modificationTime: OffsetDateTime,
    val releaseTime: OffsetDateTime?
)
