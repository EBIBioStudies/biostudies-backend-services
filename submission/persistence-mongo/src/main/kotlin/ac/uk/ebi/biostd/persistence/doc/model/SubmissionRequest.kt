package ac.uk.ebi.biostd.persistence.doc.model

import com.mongodb.DBObject
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "submission_requests")
data class SubmissionRequest(
    @Id
    val id: ObjectId,
    val accNo: String,
    val version: Int,
    val status: SubmissionRequestStatus,
    val submission: DBObject
) {
    constructor(
        accNo: String,
        version: Int,
        status: SubmissionRequestStatus,
        submission: DBObject
    ) : this(ObjectId(), accNo, version, status, submission)
}

enum class SubmissionRequestStatus { REQUESTED, PROCESSED }
