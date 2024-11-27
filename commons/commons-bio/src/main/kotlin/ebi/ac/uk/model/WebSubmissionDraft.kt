package ebi.ac.uk.model

import com.fasterxml.jackson.databind.JsonNode
import java.time.OffsetDateTime

data class WebSubmissionDraft(
    val key: String,
    val content: JsonNode,
    val modificationTime: OffsetDateTime,
)
