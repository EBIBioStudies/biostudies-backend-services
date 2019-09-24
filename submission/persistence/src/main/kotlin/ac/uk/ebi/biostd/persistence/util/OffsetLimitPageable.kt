package ac.uk.ebi.biostd.persistence.util

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class OffsetLimitPageable(private val offset: Long, private val limit: Int, private val sort: Sort) : Pageable {

    override fun getPageNumber(): Int {
        return offset.toInt() / limit
    }

    override fun getPageSize(): Int {
        return limit
    }

    override fun getOffset(): Long {
        return offset
    }

    override fun getSort(): Sort {
        return sort
    }

    override fun next(): Pageable {
        return OffsetLimitPageable(getOffset() + pageSize, pageSize, getSort())
    }

    private fun previous(): OffsetLimitPageable {
        return if (hasPrevious()) OffsetLimitPageable(getOffset() - pageSize, pageSize, getSort()) else this
    }

    override fun previousOrFirst(): Pageable {
        return if (hasPrevious()) previous() else first()
    }

    override fun first(): Pageable {
        return OffsetLimitPageable(0, pageSize, getSort())
    }

    override fun hasPrevious(): Boolean {
        return offset > limit
    }
}
