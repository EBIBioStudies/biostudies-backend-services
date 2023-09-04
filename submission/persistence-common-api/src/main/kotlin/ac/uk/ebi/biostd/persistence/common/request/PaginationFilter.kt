package ac.uk.ebi.biostd.persistence.common.request

interface PaginationFilter {
    val limit: Int
    val offset: Long
    val pageNumber: Int
        get() = (offset / limit).toInt()

    companion object {
        operator fun invoke(limit: Int, offset: Long): PaginationFilter {
            return object : PaginationFilter {
                override val limit: Int = limit
                override val offset: Long = offset
            }
        }

        operator fun invoke(): PaginationFilter = DEFAULT

        @Suppress("MagicNumber")
        private object DEFAULT : PaginationFilter {
            override val limit: Int
                get() = 15
            override val offset: Long
                get() = 0
        }
    }
}
