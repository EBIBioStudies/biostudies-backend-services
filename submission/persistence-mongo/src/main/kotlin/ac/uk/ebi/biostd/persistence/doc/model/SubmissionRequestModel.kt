package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields
import com.mongodb.DBObject
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Update
import java.time.Instant

@Document(collection = "submission_requests")
data class DocSubmissionRequest(
    @Id
    val id: ObjectId,
    val accNo: String,
    val version: Int,
    val draftKey: String?,
    val notifyTo: String,
    val status: RequestStatus,
    val submission: DBObject,
    val totalFiles: Int,
    val currentIndex: Int,
    val modificationTime: Instant,
) {

    fun asSetOnInsert(): Update {
        return Update()
            .setOnInsert(DocRequestFields.RQT_ACC_NO, accNo)
            .setOnInsert(DocRequestFields.RQT_VERSION, version)
            .setOnInsert(DocRequestFields.RQT_DRAFT_KEY, draftKey)
            .setOnInsert(DocRequestFields.RQT_NOTIFY_TO, notifyTo)
            .setOnInsert(DocRequestFields.RQT_STATUS, status)
            .setOnInsert(DocRequestFields.RQT_SUBMISSION, submission)
            .setOnInsert(DocRequestFields.RQT_TOTAL_FILES, totalFiles)
            .setOnInsert(DocRequestFields.RQT_IDX, currentIndex)
            .setOnInsert(DocRequestFields.RQT_MODIFICATION_TIME, modificationTime)
    }
}

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
