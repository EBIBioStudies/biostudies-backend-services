package ac.uk.ebi.biostd.persistence.common.model

import java.time.OffsetDateTime

data class BasicCollection(
    val accNo: String,
    val accNoPattern: String,
    val collections: List<String>,
    val validator: String?,
    val releaseTime: OffsetDateTime?,
)
