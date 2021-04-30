package ac.uk.ebi.biostd.persistence.common.request

open class PaginationFilter(open val limit: Int = 15, open val offset: Long = 0) {
    val pageNumber: Int
        get() = (offset / limit).toInt()
}
