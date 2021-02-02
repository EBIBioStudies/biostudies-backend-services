package ac.uk.ebi.biostd.persistence.common.request

import java.time.OffsetDateTime

class SubmissionFilter(
    val accNo: String? = null,
    val version: Long? = null,
    val type: String? = null,
    val rTimeFrom: OffsetDateTime? = null,
    val rTimeTo: OffsetDateTime? = null,
    val keywords: String? = null,
    val released: Boolean? = null,
    limit: Int = 15,
    offset: Long = 0
) : PaginationFilter(limit, offset)

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
        accNo = this.accNo,
        version = this.version,
        type = this.type,
        rTimeFrom = rTimeFrom?.let { OffsetDateTime.parse(it) },
        rTimeTo = rTimeTo?.let { OffsetDateTime.parse(it) },
        keywords = this.keywords,
        released = this.released,
        limit = this.limit,
        offset = this.offset
    )
}
