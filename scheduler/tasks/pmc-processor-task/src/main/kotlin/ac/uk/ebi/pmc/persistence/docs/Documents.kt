package ac.uk.ebi.pmc.persistence.docs

import ac.uk.ebi.pmc.config.ERRORS_COL
import ac.uk.ebi.pmc.config.INPUT_FILES_COL
import ac.uk.ebi.pmc.config.SUBMISSION_COL
import ac.uk.ebi.pmc.config.SUB_FILES_COL
import ac.uk.ebi.scheduler.properties.PmcMode
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = ERRORS_COL)
data class SubmissionErrorDocument(
    @Id
    val id: ObjectId = ObjectId(),
    val accNo: String? = null,
    val sourceFile: String,
    val submissionText: String,
    val error: String,
    val mode: PmcMode,
    val uploaded: Instant = Instant.now(),
) {
    companion object Fields {
        const val ERROR_SOURCE_FILE = "sourceFile"
        const val ERROR_SUB_TEXT = "submissionText"
        const val ERROR_ERROR = "error"
        const val ERROR_MODE = "mode"
        const val ERROR_UPLOADED = "uploaded"
        const val ERROR_ACCNO = "accNo"
    }
}

@Document(collection = SUBMISSION_COL)
data class SubmissionDocument(
    @Id
    val id: ObjectId = ObjectId(),
    val accNo: String,
    val version: Int? = null,
    var body: String,
    var status: SubmissionStatus,
    val sourceFile: String,
    val posInFile: Int,
    val sourceTime: Long,
    val files: List<ObjectId> = emptyList(),
    val updated: Instant = Instant.now(),
) {
    companion object Fields {
        const val SUB_ID = "_id"
        const val SUB_ACC_NO = "accNo"
        const val SUB_BODY = "body"
        const val SUB_STATUS = "status"
        const val SUB_SOURCE_FILE = "sourceFile"
        const val SUB_POS_IN_FILE = "posInFile"
        const val SUB_SOURCE_TIME = "sourceTime"
        const val SUB_UPDATED = "updated"
        const val SUB_FILES = "files"
    }
}

enum class SubmissionStatus {
    LOADED,
    PROCESSING,
    PROCESSED,
    SUBMITTING,
    SUBMITTED,
    ERROR_LOAD,
    ERROR_PROCESS,
    ERROR_SUBMIT,
    DISCARDED,
}

@Document(collection = INPUT_FILES_COL)
data class InputFileDocument(
    @Id
    val id: ObjectId = ObjectId(),
    val name: String,
    val status: InputFileStatus,
    val loaded: Instant = Instant.now(),
    val error: String?,
)

enum class InputFileStatus { PROCESSED, FAILED }

@Document(collection = SUB_FILES_COL)
data class SubFileDocument(
    @Id
    val id: ObjectId = ObjectId(),
    val name: String,
    val path: String,
    val accNo: String,
) {
    companion object Fields {
        const val FILE_DOC_PATH = "path"
        const val FILE_DOC_ACC_NO = "accNo"
    }
}
