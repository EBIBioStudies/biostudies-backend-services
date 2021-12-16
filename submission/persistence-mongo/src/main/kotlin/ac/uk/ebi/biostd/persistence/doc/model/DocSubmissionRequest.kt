package ac.uk.ebi.biostd.persistence.doc.model

import com.mongodb.DBObject
import ebi.ac.uk.extended.model.FileMode
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "submission_requests")
data class DocSubmissionRequest(
    @Id
    val id: ObjectId,
    val accNo: String,
    val version: Int,
    val fileMode: FileMode,
    val draftKey: String?,
    val status: SubmissionRequestStatus,
    val submission: DBObject
) {
    constructor(
        accNo: String,
        version: Int,
        fileMode: FileMode,
        draftKey: String?,
        status: SubmissionRequestStatus,
        submission: DBObject
    ) : this(ObjectId(), accNo, version, fileMode, draftKey, status, submission)
}

enum class SubmissionRequestStatus { REQUESTED, PROCESSED }
