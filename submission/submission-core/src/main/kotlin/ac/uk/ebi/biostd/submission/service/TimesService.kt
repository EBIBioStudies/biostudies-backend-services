package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ac.uk.ebi.biostd.submission.exceptions.InvalidReleaseException
import ac.uk.ebi.biostd.submission.exceptions.PastReleaseDateException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.util.date.atStartOfDay
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
    private val privileges: IUserPrivilegesService,
) {
    internal fun getTimes(rqt: SubmitRequest): Times {
        val now = OffsetDateTime.now()
        val creationTime = rqt.previousVersion?.creationTime ?: now
        val releaseTime = rqt.submission.releaseDate?.let { parseDate(it) }
        val released = releaseTime?.isBeforeOrEqual(now).orFalse()

        if (releaseTime != null) checkPermissions(releaseTime, rqt)
        return Times(creationTime, now, releaseTime, released)
    }

    private fun checkPermissions(
        releaseTime: OffsetDateTime,
        rqt: SubmitRequest,
    ) {
        val submitter = rqt.submitter.email
        val today = OffsetDateTime.now().atStartOfDay()

        when (val previous = rqt.previousVersion) {
            null -> if (releaseTime < today) throw PastReleaseDateException()
            else ->
                if (previous.released) {
                    if (releaseTime > today && privileges.canSuppress(submitter).not()) throw InvalidReleaseException()
                    if (releaseTime != previous.releaseTime && releaseTime < today) throw InvalidReleaseException()
                }
        }
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
