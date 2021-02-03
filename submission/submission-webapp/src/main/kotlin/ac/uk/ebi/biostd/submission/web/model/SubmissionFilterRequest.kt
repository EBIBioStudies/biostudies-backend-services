package ac.uk.ebi.biostd.submission.web.model

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import java.time.OffsetDateTime

class SubmissionFilterRequest(
    val accNo: String? = null,
    val version: Long? = null,
    val type: String? = null,
    val rTimeFrom: String? = null,
    val rTimeTo: String? = null,
    val keywords: String? = null,
    val released: Boolean? = null,
    val limit: Int = 15,
    val offset: Long = 0
)

fun SubmissionFilterRequest.asFilter(): SubmissionFilter {
    return SubmissionFilter(
        accNo = accNo,
        version = version,
        type = type,
        rTimeFrom = rTimeFrom?.let { OffsetDateTime.parse(it) },
        rTimeTo = rTimeTo?.let { OffsetDateTime.parse(it) },
        keywords = keywords,
        released = released,
        limit = limit,
        offset = offset
    )
}
