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
    val submission: DBObject,
)

data class RequestFileList(val fileName: String, val filePath: String)
enum class SubmissionRequestStatus { REQUESTED, PROCESSED }
