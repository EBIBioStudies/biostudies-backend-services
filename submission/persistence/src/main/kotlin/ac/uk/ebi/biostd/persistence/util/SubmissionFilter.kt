package ac.uk.ebi.biostd.persistence.util

import java.time.OffsetDateTime

data class SubmissionFilter(
    val accNo: String? = null,
    val version: Long? = null,
    val rTimeFrom: OffsetDateTime? = null,
    val rTimeTo: OffsetDateTime? = null,
    val keywords: String? = null,
    val limit: Int = 15,
    val offset: Int = 0
) {
    val pageNumber: Int
        get() = offset / limit
}
