package ac.uk.ebi.biostd.persistence.doc.model

import org.json.JSONObject
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "submission-requests")
data class SubmissionRequest(
    @Id
    val id: String? = null,
    val accNo: String,
    val version: Int,
    val request: JSONObject
)
