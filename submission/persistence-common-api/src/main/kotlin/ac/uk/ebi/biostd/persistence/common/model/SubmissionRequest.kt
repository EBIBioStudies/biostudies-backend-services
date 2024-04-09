package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtSubmission
import java.time.Instant
import java.time.OffsetDateTime

data class RequestStatusChanges(
    val status: String,
    val changeId: String,
    val processId: String,
    val startTime: Instant,
    val endTime: Instant?,
)

data class SubmissionRequest(
    val submission: ExtSubmission,
    val draftKey: String?,
    val notifyTo: String,
    val status: RequestStatus,
    val totalFiles: Int,
    val currentIndex: Int,
    val modificationTime: OffsetDateTime,
) {
    constructor(submission: ExtSubmission, notifyTo: String, draftKey: String? = null) : this(
        submission,
        draftKey,
        notifyTo,
        status = RequestStatus.REQUESTED,
        totalFiles = 0,
        currentIndex = 0,
        modificationTime = OffsetDateTime.now(),
    )

    /**
     * Update request by setting new status, resetting current Index and updating modification date.
     */
    fun withNewStatus(status: RequestStatus): SubmissionRequest {
        return copy(
            status = status,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
        )
    }

    fun indexed(totalFiles: Int): SubmissionRequest {
        return copy(
            status = RequestStatus.INDEXED,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            totalFiles = totalFiles,
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
    PROCESSED,
    ;

    companion object {
        val PROCESSING: Set<RequestStatus> =
            setOf(
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
            else -> error("Invalid state $this")
        }
    }
