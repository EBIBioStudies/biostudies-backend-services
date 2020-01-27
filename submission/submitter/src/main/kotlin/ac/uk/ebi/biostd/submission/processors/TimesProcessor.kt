package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.addAccessTag
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.util.date.isBeforeOrEqual
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
class TimesProcessor : SubmissionProcessor {

    override fun process(submission: ExtendedSubmission, context: PersistenceContext) {
        val now = OffsetDateTime.now()
        submission.modificationTime = now
        submission.creationTime = context.getSubmission(submission.accNo)?.creationTime ?: now
        submission.releaseTime = releaseTime(submission, context, now)
        submission.releaseDate = null

        if (submission.releaseTime?.isBeforeOrEqual(now).orFalse()) {
            submission.released = true
            submission.addAccessTag(SubFields.PUBLIC_ACCESS_TAG.value)
        }
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
}
