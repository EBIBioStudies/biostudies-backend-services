package ac.uk.ebi.biostd.persistence.common.request

import java.time.OffsetDateTime

sealed interface SubFilter : PaginationFilter {
    val rTimeFrom: OffsetDateTime?
    val rTimeTo: OffsetDateTime?
    val notIncludeAccNo: Set<String>?
    val collection: String?
    val released: Boolean?
}

data class SimpleFilter(
    override val rTimeFrom: OffsetDateTime? = null,
    override val rTimeTo: OffsetDateTime? = null,
    override val notIncludeAccNo: Set<String>? = null,
    override val collection: String?,
    override val released: Boolean?,
    override val limit: Int = 15,
    override val offset: Long = 0,
) : SubFilter

data class SubmissionFilter(
    val filterUser: String,
    val findAnyAccNo: Boolean = false,
    val accNo: String? = null,
    val keywords: String? = null,
    val version: Long? = null,
    val type: String? = null,


    override val rTimeFrom: OffsetDateTime? = null,
    override val rTimeTo: OffsetDateTime? = null,
    override val released: Boolean? = null,
    override val collection: String? = null,
    override val notIncludeAccNo: Set<String>? = null,
    override val limit: Int = 15,
    override val offset: Long = 0,
) : SubFilter
