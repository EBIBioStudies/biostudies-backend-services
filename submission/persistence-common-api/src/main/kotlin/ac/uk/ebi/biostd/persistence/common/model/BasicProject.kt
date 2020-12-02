package ac.uk.ebi.biostd.persistence.common.model

import java.time.OffsetDateTime

data class BasicProject(
    val accNo: String,
    val accNoPattern: String,
    val releaseTime: OffsetDateTime?
)
