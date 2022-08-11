package ac.uk.ebi.biostd.persistence.doc.model

import com.mongodb.DBObject
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "submission_requests")
data class DocSubmissionRequest(
    @Id
    val id: ObjectId,
    val accNo: String,
    val version: Int,
    val draftKey: String?,
    val status: SubmissionRequestStatus,
    val submission: DBObject,
)

enum class SubmissionRequestStatus { REQUESTED, PROCESSED }
