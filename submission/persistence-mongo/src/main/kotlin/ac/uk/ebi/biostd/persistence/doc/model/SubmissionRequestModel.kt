package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields
import ac.uk.ebi.biostd.persistence.doc.model.CollectionNames.SUB_RQT
import ac.uk.ebi.biostd.persistence.doc.model.CollectionNames.SUB_RQT_FILES
import com.mongodb.DBObject
import ebi.ac.uk.model.RequestStatus
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Update
import java.time.Instant

@Document(collection = SUB_RQT)
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
    val fileChanges: DocFilesChanges,
    val currentIndex: Int,
    val previousVersion: Int?,
    val modificationTime: Instant,
    val silentMode: Boolean,
    val singleJobMode: Boolean,
    val statusChanges: List<DocRequestStatusChanges> = emptyList(),
) {
    fun asSetOnInsert(): Update =
        Update()
            .setOnInsert("_id", id)
            .setOnInsert(DocRequestFields.RQT_ACC_NO, accNo)
            .setOnInsert(DocRequestFields.RQT_VERSION, version)
            .setOnInsert(DocRequestFields.RQT_DRAFT_KEY, draftKey)
            .setOnInsert(DocRequestFields.RQT_NOTIFY_TO, notifyTo)
            .setOnInsert(DocRequestFields.RQT_STATUS, status)
            .setOnInsert(DocRequestFields.RQT_SUBMISSION, submission)
            .setOnInsert(DocRequestFields.RQT_TOTAL_FILES, totalFiles)
            .setOnInsert(DocRequestFields.RQT_FILE_CHANGES, fileChanges)
            .setOnInsert(DocRequestFields.RQT_PREV_SUB_VERSION, previousVersion)
            .setOnInsert(DocRequestFields.RQT_IDX, currentIndex)
            .setOnInsert(DocRequestFields.RQT_MODIFICATION_TIME, modificationTime)
            .setOnInsert(DocRequestFields.RQT_SILENT_MODE, silentMode)
            .setOnInsert(DocRequestFields.RQT_SINGLE_JOB_MODE, singleJobMode)
            .setOnInsert(DocRequestFields.RQT_STATUS_CHANGES, statusChanges)
}

data class DocFilesChanges(
    val conflictingFiles: Int,
    val conflictingPageTab: Int,
    val deprecatedFiles: Int,
    val deprecatedPageTab: Int,
    val reusedFiles: Int,
)

data class DocRequestStatusChanges(
    val status: String,
    val statusId: ObjectId,
    val processId: String,
    val startTime: Instant,
    val endTime: Instant?,
    val result: String?,
)

@Document(collection = SUB_RQT_FILES)
data class DocSubmissionRequestFile(
    @Id
    val id: ObjectId,
    val index: Int,
    val accNo: String,
    val version: Int,
    val path: String,
    val file: DBObject,
    val status: RequestFileStatus,
    val previousSubFile: Boolean,
)
