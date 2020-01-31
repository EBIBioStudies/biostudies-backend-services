package ac.uk.ebi.biostd.submission.processing

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.util.date.max
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Calculates the submission release date based on current state of the submission in the system. Calculation rules
 *
 */
internal fun SubmissionSubmitService.getTimes(submission: ExtendedSubmission): Times {
    val now = OffsetDateTime.now()
    val creationTime = context.getSubmission(submission.accNo)?.creationTime ?: now
    val releaseTime = releaseTime(submission, context, now)
    return Times(creationTime, now, releaseTime)
}

private fun releaseTime(sub: ExtendedSubmission, ctx: PersistenceContext, now: OffsetDateTime): OffsetDateTime? {
    val releaseTime = sub.releaseDate?.let { parseDate(it) } ?: now
    return when {
        ctx.hasParent(sub) -> ctx.getParentReleaseTime(sub)?.let { max(it, releaseTime) }
        else -> releaseTime
    }
}

private fun parseDate(date: String): OffsetDateTime =
    runCatching { LocalDate.parse(date) }
        .recoverCatching { Instant.parse(date).atZone(ZoneId.systemDefault()).toLocalDate() }
        .fold({ it.atStartOfDay().atOffset(ZoneOffset.UTC) }, { throw InvalidDateFormatException(date) })
