package ac.uk.ebi.biostd.persistence.pagination

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

@Suppress("ConstructorParameterNaming")
class OffsetPageRequest(
    private val _offset: Long,
    private val _limit: Int,
    private val _sort: Sort
) : Pageable {
    override fun getPageNumber(): Int = _offset.toInt() / _limit
    override fun getPageSize(): Int = _limit
    override fun getOffset(): Long = _offset
    override fun getSort(): Sort = _sort
    override fun next(): Pageable = OffsetPageRequest(_offset + _limit, _limit, _sort)
    override fun previousOrFirst() = if (hasPrevious()) OffsetPageRequest(_offset - _limit, _limit, _sort) else first()
    override fun first(): Pageable = OffsetPageRequest(0, _limit, _sort)
    override fun hasPrevious(): Boolean = _offset > _limit
}
