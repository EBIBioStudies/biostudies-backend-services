package ac.uk.ebi.biostd.persistence.util

data class SubmissionFilter(
    val accNo: String? = null,
    val version: Long? = null,
    val rTimeFrom: String? = null,
    val rTimeTo: String? = null,
    val keywords: String? = null,
    val limit: Int = 15,
    val offset: Int = 0
) {
    val pageNumber: Int
        get() = offset / limit
}
