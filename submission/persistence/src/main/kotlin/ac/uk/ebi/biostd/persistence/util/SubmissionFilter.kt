package ac.uk.ebi.biostd.persistence.util

data class SubmissionFilter(
    val version: Long? = null,
    val rTimeFrom: Long? = null,
    val rTimeTo: Long? = null,
    val accNo: String? = null,
    val keywords: String? = null,
    val limit: Int = 15,
    val offset: Int = 0
)
