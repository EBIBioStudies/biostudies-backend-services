package ac.uk.ebi.biostd.persistence.common.request

import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime

data class PageRequest(
    val limit: Int = 15,
    val offset: Long = 0,
) {
    val pageNumber: Int
        get() = (offset / limit).toInt()

    fun asDataPageRequest(): PageRequest {
        return PageRequest.of(pageNumber, limit)
    }
}

sealed interface SubmissionFilter {
    val rTimeFrom: OffsetDateTime?
    val rTimeTo: OffsetDateTime?
    val notIncludeAccNo: Set<String>?
    val collection: String?
    val released: Boolean?
    val limit: Int
    val offset: Long

    val pageNumber: Int
        get() = (offset / limit).toInt()
}

data class SimpleFilter(
    override val rTimeFrom: OffsetDateTime? = null,
    override val rTimeTo: OffsetDateTime? = null,
    override val notIncludeAccNo: Set<String>? = null,
    override val collection: String?,
    override val released: Boolean?,
    override val limit: Int = 15,
    override val offset: Long = 0,
) : SubmissionFilter

data class SubmissionListFilter(
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
) : SubmissionFilter
