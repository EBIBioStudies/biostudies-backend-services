package ac.uk.ebi.biostd.submission.processing

import java.time.OffsetDateTime

data class Times(
    val createTime: OffsetDateTime,
    val modificationTime: OffsetDateTime,
    val releaseTime: OffsetDateTime?
)
