package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ac.uk.ebi.biostd.submission.exceptions.PastReleaseDateException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUnrelease
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.util.date.asOffsetAtStartOfDay
import ebi.ac.uk.util.date.isBeforeOrEqual
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC

/**
 * Calculates the submission release date based on current state of the submission in the system. Calculation rules.
 */
class TimesService(
    private val privilegesService: IUserPrivilegesService,
) {
    internal fun getTimes(request: SubmitRequest): Times {
        val now = OffsetDateTime.now()
        val creationTime = request.previousVersion?.creationTime ?: now
        val releaseTime = request.submission.releaseDate?.let { releaseTime(now, it, request) }
        val released = releaseTime?.isBeforeOrEqual(now).orFalse()

        return Times(creationTime, now, releaseTime, released)
    }

    private fun releaseTime(
        now: OffsetDateTime,
        releaseDate: String,
        request: SubmitRequest,
    ): OffsetDateTime {
        val releaseTime = parseDate(releaseDate)
        val accNo = request.accNo
        val user = request.submitter.email
        val today = now.toInstant().asOffsetAtStartOfDay()
        val isReleased = request.previousVersion?.released.orFalse()

        return when {
            releaseTime.isBefore(today) -> throw PastReleaseDateException()
            releaseTime.isAfter(today) && isReleased && cantUnrelease(user) -> throw UserCanNotUnrelease(accNo, user)
            else -> releaseTime
        }
    }

    private fun cantUnrelease(user: String) = privilegesService.canUnrelease(user).not()

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
