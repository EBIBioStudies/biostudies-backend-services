package ac.uk.ebi.biostd.persistence.common.request

class SubmissionFilter(
    val accNo: String? = null,
    val version: Long? = null,
    val type: String? = null,
    val rTimeFrom: String? = null,
    val rTimeTo: String? = null,
    val keywords: String? = null,
    val released: Boolean? = null,
    limit: Int = 15,
    offset: Int = 0
) : PaginationFilter(limit, offset)
