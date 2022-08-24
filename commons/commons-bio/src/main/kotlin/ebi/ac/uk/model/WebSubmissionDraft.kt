package ebi.ac.uk.model

import com.fasterxml.jackson.databind.JsonNode

data class WebSubmissionDraft(val key: String, val content: JsonNode)
