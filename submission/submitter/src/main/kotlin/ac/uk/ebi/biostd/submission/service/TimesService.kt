package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.util.date.isBeforeOrEqual
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC

/**
 * Calculates the submission release date based on current state of the submission in the system. Calculation rules.
 */
class TimesService {
    internal fun getTimes(request: SubmitRequest): Times {
        val now = OffsetDateTime.now()
        val creationTime = request.previousVersion?.creationTime ?: now
        val releaseTime = request.submission.releaseDate?.let { parseDate(it) }
        val released = releaseTime?.isBeforeOrEqual(now).orFalse()

        return Times(creationTime, now, releaseTime, released)
    }

    private fun parseDate(date: String): OffsetDateTime =
        runCatching { LocalDate.parse(date) }
            .recoverCatching { Instant.parse(date).atZone(ZoneId.systemDefault()).toLocalDate() }
            .fold({ it.atStartOfDay().atOffset(UTC) }, { throw InvalidDateFormatException(date) })
}

data class Times(
    val createTime: OffsetDateTime,
    val modificationTime: OffsetDateTime,
    val releaseTime: OffsetDateTime?,
    val released: Boolean,
)
