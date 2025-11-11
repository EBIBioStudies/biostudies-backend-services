package ac.uk.ebi.biostd.persistence.common.request

import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime

data class PageRequest(
    val limit: Int = 15,
    val offset: Long = 0,
) {
    val pageNumber: Int
        get() = (offset / limit).toInt()

    fun asDataPageRequest(): PageRequest = PageRequest.of(pageNumber, limit)
}

sealed interface SubmissionFilter {
    val limit: Int
    val offset: Long

    val pageNumber: Int
        get() = (offset / limit).toInt()
}

data class SimpleFilter(
    val rTimeFrom: OffsetDateTime?,
    val rTimeTo: OffsetDateTime?,
    val collection: String?,
    val released: Boolean?,
    override val limit: Int = 15,
    override val offset: Long = 0,
) : SubmissionFilter

data class ListFilter(
    val filterUser: String,
    val findAnyAccNo: Boolean = false,
    val accNo: String? = null,
    val adminCollections: List<String>? = null,
    val notIncludeAccNo: Set<String> = emptySet(),
    override val limit: Int = 15,
    override val offset: Long = 0,
) : SubmissionFilter
