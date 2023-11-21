package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.util.collections.replace
import java.time.Instant
import java.time.OffsetDateTime

data class RequestStatusChanges(
    val status: String,
    val changeId: String,
    val processId: String,
    val startTime: Instant,
    val endTime: Instant?,
)

data class SubmissionRequest constructor(
    val submission: ExtSubmission,
    val draftKey: String?,
    val notifyTo: String,
    val status: RequestStatus,
    val totalFiles: Int,
    val currentIndex: Int,
    val modificationTime: OffsetDateTime,
    val statusChanges: List<RequestStatusChanges>,
) {

    constructor(submission: ExtSubmission, notifyTo: String, draftKey: String? = null) : this(
        submission,
        draftKey,
        notifyTo,
        status = RequestStatus.REQUESTED,
        totalFiles = 0,
        currentIndex = 0,
        modificationTime = OffsetDateTime.now(),
        statusChanges = emptyList()
    )

    /**
     * Update request by setting new status, resetting current Index and updating modification date.
     * Optionally total files can be updated.
     */
    fun withNewStatus(status: RequestStatus, changeId: String): SubmissionRequest {
        val statusChange = statusChanges
            .filter { it.changeId == changeId }
            .first()
            .copy(endTime = Instant.now())
        return copy(
            status = status,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            statusChanges = statusChanges.replace(statusChange, { it.changeId == changeId })
        )
    }

    fun indexed(totalFiles: Int, changeId: String): SubmissionRequest {
        val statusChange = statusChanges
            .filter { it.changeId == changeId }
            .first()
            .copy(endTime = Instant.now())
        return copy(
            status = RequestStatus.INDEXED,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            totalFiles = totalFiles,
            statusChanges = statusChanges.replace(statusChange, { it.changeId == changeId })
        )
    }
}

enum class RequestProcess {
    CREATE_REQUEST,
    INDEX,
    LOAD,
    CLEAN,
    COPY_FILES,
    CHECK_RELEASE,
    PERSIST,
    PROCESS
}

enum class RequestStatus {
    REQUESTED,
    INDEXED,
    LOADED,
    CLEANED,
    FILES_COPIED,
    CHECK_RELEASED,
    PERSISTED,
    PROCESSED;

    companion object {
        val PROCESSING: Set<RequestStatus> = setOf(
            REQUESTED,
            INDEXED,
            LOADED,
            CLEANED,
            FILES_COPIED,
            CHECK_RELEASED,
            PERSISTED,
        )
    }
}

val RequestStatus.nextStatus: String
    get() {
        return when (this) {
            RequestStatus.REQUESTED -> "Indexing"
            RequestStatus.INDEXED -> "Loading"
            RequestStatus.LOADED -> "Cleaning"
            RequestStatus.CLEANED -> "Copy Files"
            RequestStatus.FILES_COPIED -> "Release Files"
            RequestStatus.CHECK_RELEASED -> "Save Submission"
            RequestStatus.PERSISTED -> "Submission Post Processing"
            else -> throw IllegalStateException("Invalid state $this")
        }
    }
