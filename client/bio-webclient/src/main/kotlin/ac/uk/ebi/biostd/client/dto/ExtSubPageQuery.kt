package ac.uk.ebi.biostd.client.dto

import java.time.OffsetDateTime

data class ExtSubPageQuery(
    val limit: Int = 15,
    val offset: Int = 0,
    val fromRTime: OffsetDateTime? = null,
    val toRTime: OffsetDateTime? = null,
    val released: Boolean? = null,
    val collection: String? = null,
)

data class ExtPageRequest(
    val limit: Int = 15,
    val offset: Int = 0,
)
