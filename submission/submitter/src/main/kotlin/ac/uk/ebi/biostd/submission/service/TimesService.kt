package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.util.date.max
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Calculates the submission release date based on current state of the submission in the system. Calculation rules.
 */
class TimesService(private val context: PersistenceContext) {
    internal fun getTimes(request: TimesRequest): Times {
        val now = OffsetDateTime.now()
        val creationTime = context.getSubmission(request.accNo)?.creationTime ?: now
        val releaseTime = releaseTime(request, now)
        return Times(creationTime, now, releaseTime)
    }

    private fun releaseTime(request: TimesRequest, now: OffsetDateTime): OffsetDateTime? {
        val releaseTime = request.releaseDate?.let { parseDate(it) } ?: now
        return when {
            request.parentReleaseTime != null -> max(request.parentReleaseTime, releaseTime)
            else -> releaseTime
        }
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

data class TimesRequest(
    val accNo: String,
    val releaseDate: String? = null,
    val parentReleaseTime: OffsetDateTime? = null
)
