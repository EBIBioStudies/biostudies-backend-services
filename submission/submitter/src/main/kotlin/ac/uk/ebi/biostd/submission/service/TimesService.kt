package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.util.date.isBeforeOrEqual
import java.time.OffsetDateTime

/**
 * Calculates the submission release date based on current state of the submission in the system. Calculation rules.
 */
class TimesService {
    internal fun getTimes(request: SubmitRequest): Times {
        val now = OffsetDateTime.now()
        val creationTime = request.previousVersion?.creationTime ?: now
        val releaseTime = request.collection?.releaseTime
        val released = releaseTime?.isBeforeOrEqual(now).orFalse()

        return Times(creationTime, now, releaseTime, released)
    }
}

data class Times(
    val createTime: OffsetDateTime,
    val modificationTime: OffsetDateTime,
    val releaseTime: OffsetDateTime?,
    val released: Boolean,
)
