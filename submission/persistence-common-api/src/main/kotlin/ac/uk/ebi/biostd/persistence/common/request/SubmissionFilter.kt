package ac.uk.ebi.biostd.persistence.common.request

import java.time.OffsetDateTime

data class SubmissionFilter(
    val accNo: String? = null,
    val version: Long? = null,
    val type: String? = null,
    val rTimeFrom: OffsetDateTime? = null,
    val rTimeTo: OffsetDateTime? = null,
    val keywords: String? = null,
    val released: Boolean? = null,
    val collection: String? = null,
    val notIncludeAccNo: Set<String>? = null,
    override val limit: Int = 15,
    override val offset: Long = 0
) : PaginationFilter(limit, offset)
