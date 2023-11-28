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
    val statusChangesLog: List<RequestStatusChanges>,
) {

    constructor(submission: ExtSubmission, notifyTo: String, draftKey: String? = null) : this(
        submission,
        draftKey,
        notifyTo,
        status = RequestStatus.REQUESTED,
        totalFiles = 0,
        currentIndex = 0,
        modificationTime = OffsetDateTime.now(),
        statusChangesLog = emptyList()
    )

    /**
     * Update request by setting new status, resetting current Index and updating modification date.
     *  Recieve the specific change Id to update endtime.
     */
    fun withNewStatus(status: RequestStatus, changeId: String): SubmissionRequest {
        val statusChange = statusChangesLog
            .filter { it.changeId == changeId }
            .first()
            .copy(endTime = Instant.now())
        return copy(
            status = status,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            statusChangesLog = statusChangesLog.replace(statusChange, { it.changeId == changeId })
        )
    }

    fun indexed(totalFiles: Int, changeId: String): SubmissionRequest {
        val statusChange = statusChangesLog
            .filter { it.changeId == changeId }
            .first()
            .copy(endTime = Instant.now())
        return copy(
            status = RequestStatus.INDEXED,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            totalFiles = totalFiles,
            statusChangesLog = statusChangesLog.replace(statusChange, { it.changeId == changeId })
        )
    }
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

/**
 * Retrieves the expected action to be perform when submission request is the given status.
 */
val RequestStatus.action: String
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
