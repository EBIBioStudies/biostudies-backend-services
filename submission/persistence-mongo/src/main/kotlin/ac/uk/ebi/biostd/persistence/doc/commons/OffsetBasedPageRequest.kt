package ac.uk.ebi.biostd.persistence.doc.commons

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class OffsetBasedPageRequest(
    private val offset: Long,
    private val limit: Int,
    private val sort: Sort = Sort.unsorted(),
) : Pageable {
    override fun getPageNumber(): Int = (offset / limit).toInt()

    override fun getPageSize(): Int = limit

    override fun getOffset(): Long = offset

    override fun getSort(): Sort = sort

    override fun next(): Pageable = OffsetBasedPageRequest(offset + limit, limit, sort)

    override fun previousOrFirst(): Pageable = OffsetBasedPageRequest((offset - limit).coerceAtLeast(0), limit, sort)

    override fun first(): Pageable = OffsetBasedPageRequest(0, limit, sort)

    override fun withPage(pageNumber: Int): Pageable = OffsetBasedPageRequest(pageNumber * limit.toLong(), limit, sort)

    override fun hasPrevious(): Boolean = offset > 0

    companion object {
        fun fromOffsetAndLimit(
            offset: Long,
            limit: Int,
        ): Pageable = OffsetBasedPageRequest(offset, limit)
    }
}
