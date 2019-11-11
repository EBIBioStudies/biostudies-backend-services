package ac.uk.ebi.biostd.persistence.filter

open class PaginationFilter(val limit: Int = 15, private val offset: Int = 0) {
    val pageNumber: Int
        get() = offset / limit
}
