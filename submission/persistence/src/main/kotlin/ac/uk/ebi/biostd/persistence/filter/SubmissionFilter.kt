package ac.uk.ebi.biostd.persistence.filter

class SubmissionFilter(
    val accNo: String? = null,
    val version: Long? = null,
    val rTimeFrom: String? = null,
    val rTimeTo: String? = null,
    val keywords: String? = null,
    limit: Int = 15,
    offset: Int = 0
) : PaginationFilter(limit, offset)
