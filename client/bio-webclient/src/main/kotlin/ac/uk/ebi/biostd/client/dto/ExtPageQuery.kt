package ac.uk.ebi.biostd.client.dto

import java.time.OffsetDateTime

data class ExtPageQuery(
    val limit: Int = 15,
    val offset: Int = 0,
    val fromRTime: OffsetDateTime?,
    val toRTime: OffsetDateTime?
)
