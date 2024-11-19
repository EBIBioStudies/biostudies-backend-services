package ac.uk.ebi.biostd.persistence.common.model

import java.time.OffsetDateTime

class SubmissionDraft(
    val key: String,
    val content: String,
    val modificationTime: OffsetDateTime,
)
