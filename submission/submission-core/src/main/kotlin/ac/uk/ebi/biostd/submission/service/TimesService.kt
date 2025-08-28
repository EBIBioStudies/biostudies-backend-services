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
 * Calculates the submission release date based on the current state of the submission in the system. Calculation rules.
 */
class TimesService(
    private val privileges: IUserPrivilegesService,
) {
    internal fun getTimes(rqt: SubmitRequest): Times {
        val now = OffsetDateTime.now()
        val creationTime = rqt.previousVersion?.creationTime ?: now
        val releaseTime = rqt.submission.releaseDate?.let { parseDate(it) }
        val released = releaseTime?.isBeforeOrEqual(now).orFalse()
        val submitter = rqt.submitter.email
        val collection = rqt.collection?.accNo

        if (releaseTime != null && privileges.canUpdateReleaseDate(submitter, collection).not()) {
            checkReleaseTime(
                rqt,
                releaseTime,
            )
        }
        return Times(creationTime, now, releaseTime, released)
    }

    @Suppress("ThrowsCount")
    private fun checkReleaseTime(
        rqt: SubmitRequest,
        releaseTime: OffsetDateTime,
    ) {
        val today = OffsetDateTime.now().atStartOfDay()

        when (val previous = rqt.previousVersion) {
            null -> if (releaseTime < today) throw PastReleaseDateException()
            else -> if (previous.released && releaseTime != previous.releaseTime) throw InvalidReleaseException()
        }
    }

    private fun parseDate(date: String): OffsetDateTime =
        runCatching { LocalDate.parse(date) }
            .recoverCatching { Instant.parse(date).atZone(ZoneId.systemDefault()).toLocalDate() }
            .fold({ it.atStartOfDay().atOffset(UTC) }, { throw InvalidDateFormatException(date) })
}

data class Times(
    val createTime: OffsetDateTime,
    val submissionTime: OffsetDateTime,
    val releaseTime: OffsetDateTime?,
    val released: Boolean,
)
