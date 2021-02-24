package ac.uk.ebi.biostd.persistence.common.model

import java.time.OffsetDateTime

data class BasicCollection(
    val accNo: String,
    val accNoPattern: String,
    val releaseTime: OffsetDateTime?
)
