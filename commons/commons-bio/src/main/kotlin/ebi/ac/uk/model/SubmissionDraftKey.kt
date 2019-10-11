package ebi.ac.uk.model

import com.fasterxml.jackson.databind.JsonNode

class SubmissionDraftKey(val accno: String)

class SubmissionDraft(val key: String, val value: JsonNode)
