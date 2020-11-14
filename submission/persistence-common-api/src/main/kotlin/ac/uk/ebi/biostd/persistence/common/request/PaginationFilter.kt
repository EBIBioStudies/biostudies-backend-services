package ac.uk.ebi.biostd.persistence.common.request

open class PaginationFilter(val limit: Int = 15, val offset: Int = 0) {
    val pageNumber: Int
        get() = offset / limit
}
