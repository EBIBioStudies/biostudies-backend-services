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
