package ac.uk.ebi.biostd.persistence.filter

open class PaginationFilter(val limit: Int = 15, val offset: Int = 0) {
    val pageNumber: Int
        get() = offset / limit
}
