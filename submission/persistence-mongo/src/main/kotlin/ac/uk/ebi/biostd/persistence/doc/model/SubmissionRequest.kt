package ac.uk.ebi.biostd.persistence.doc.model

import com.mongodb.DBObject
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document(collection = "submission_requests")
data class SubmissionRequest(
    @Id
    val id: String? = null,
    val accNo: String,
    val version: Int,
    val status: SubmissionRequestStatus,
    val submission: DBObject
)

enum class SubmissionRequestStatus { REQUESTED, PROCESSED }
