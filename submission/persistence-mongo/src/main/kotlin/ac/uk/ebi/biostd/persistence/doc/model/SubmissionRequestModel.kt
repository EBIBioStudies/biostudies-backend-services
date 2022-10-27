package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import com.mongodb.DBObject
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "submission_requests")
data class DocSubmissionRequest(
    @Id
    val id: ObjectId,
    val accNo: String,
    val version: Int,
    val draftKey: String?,
    val status: RequestStatus,
    val submission: DBObject,
    val totalFiles: Int,
    val currentIndex: Int,
    val modificationTime: Instant,
)

@Document(collection = "submission_request_files")
data class DocSubmissionRequestFile(
    @Id
    val id: ObjectId,
    val index: Int,
    val accNo: String,
    val version: Int,
    val path: String,
    val file: DBObject,
)
