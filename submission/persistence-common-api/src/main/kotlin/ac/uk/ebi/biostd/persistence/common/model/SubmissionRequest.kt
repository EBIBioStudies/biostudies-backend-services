package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtSubmission
import java.time.OffsetDateTime

data class SubmissionRequest(
    val submission: ExtSubmission,
    val draftKey: String?,
    val notifyTo: String,
    val status: RequestStatus,
    val totalFiles: Int,
    val conflictedFiles: Int,
    val deprecatedFiles: Int,
    val currentIndex: Int,
    val modificationTime: OffsetDateTime,
    val previousVersion: Int?,
) {
    constructor(submission: ExtSubmission, notifyTo: String, draftKey: String? = null) : this(
        submission,
        draftKey,
        notifyTo,
        status = RequestStatus.REQUESTED,
        totalFiles = 0,
        conflictedFiles = 0,
        deprecatedFiles = 0,
        currentIndex = 0,
        previousVersion = null,
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

    /**
     * Create a Submission Request after indexing stage setting total files field.
     */
    fun indexed(totalFiles: Int): SubmissionRequest {
        return copy(
            status = RequestStatus.INDEXED,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            totalFiles = totalFiles,
        )
    }

    /**
     * Create a Submission Request after clean indexing stage setting conflicted, deprecated files and previous version
     * fields.
     */
    fun cleanIndexed(
        conflictedFiles: Int,
        deprecatedFiles: Int,
        previousVersion: Int?,
    ): SubmissionRequest {
        return copy(
            status = RequestStatus.INDEXED_CLEANED,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            conflictedFiles = conflictedFiles,
            deprecatedFiles = deprecatedFiles,
            previousVersion = previousVersion,
        )
    }
}

enum class RequestStatus {
    REQUESTED,
    INDEXED,
    LOADED,
    INDEXED_CLEANED,
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
                INDEXED_CLEANED,
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
            RequestStatus.LOADED -> "Indexing To Clean Files"
            RequestStatus.INDEXED_CLEANED -> "Cleaning"
            RequestStatus.CLEANED -> "Copy Files"
            RequestStatus.FILES_COPIED -> "Release Files"
            RequestStatus.CHECK_RELEASED -> "Save Submission"
            RequestStatus.PERSISTED -> "Submission Post Processing"
            else -> error("Invalid state $this")
        }
    }
