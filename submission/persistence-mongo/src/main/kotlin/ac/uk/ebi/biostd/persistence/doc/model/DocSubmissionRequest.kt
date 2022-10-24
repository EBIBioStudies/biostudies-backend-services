package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import com.mongodb.DBObject
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * TODO: Remove default parameters when all submission request has been processed.
 */
@Document(collection = "submission_requests")
data class DocSubmissionRequest(
    @Id
    val id: ObjectId,
    val accNo: String,
    val version: Int,
    val draftKey: String?,
    val status: RequestStatus,
    val submission: DBObject,
    val totalFiles: Int = 0,
    val currentIndex: Int = 0,
    val modificationTime: Instant = Instant.now(),
)
