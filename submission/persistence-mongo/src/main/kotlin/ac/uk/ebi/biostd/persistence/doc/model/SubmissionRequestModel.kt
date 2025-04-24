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
    val owner: String,
    val draft: String?,
    val status: RequestStatus,
    val modificationTime: Instant,
    val process: DocRequestProcessing?,
    val errors: List<String> = emptyList(),
) {
    fun asUpsert(): Update =
        Update()
            .setOnInsert("_id", id)
            .setOnInsert(DocRequestFields.RQT_ACC_NO, accNo)
            .setOnInsert(DocRequestFields.RQT_OWNER, owner)
            .setOnInsert(DocRequestFields.RQT_DRAFT, draft)
            .set(DocRequestFields.RQT_VERSION, version)
            .set(DocRequestFields.RQT_STATUS, status)
            .set(DocRequestFields.RQT_ERRORS, errors)
            .set(DocRequestFields.RQT_PROCESS, process)
            .set(DocRequestFields.RQT_MODIFICATION_TIME, modificationTime)
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

data class DocRequestProcessing(
    val submission: DBObject,
    val notifyTo: String,
    val totalFiles: Int,
    val fileChanges: DocFilesChanges,
    val currentIndex: Int,
    val silentMode: Boolean,
    val singleJobMode: Boolean,
    val previousVersion: Int?,
    val statusChanges: List<DocRequestStatusChanges> = emptyList(),
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
