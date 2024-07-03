package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.REQUESTED
import java.time.OffsetDateTime

data class SubmissionRequest(
    val submission: ExtSubmission,
    val draftKey: String?,
    val notifyTo: String,
    val status: RequestStatus,
    val totalFiles: Int,
    val conflictingFiles: Int,
    val deprecatedFiles: Int,
    val reusedFiles: Int,
    val currentIndex: Int,
    val modificationTime: OffsetDateTime,
    val previousVersion: Int?,
) {
    constructor(submission: ExtSubmission, notifyTo: String, draftKey: String? = null) : this(
        submission,
        draftKey,
        notifyTo,
        status = REQUESTED,
        totalFiles = 0,
        conflictingFiles = 0,
        deprecatedFiles = 0,
        reusedFiles = 0,
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
        conflictingFiles: Int,
        deprecatedFiles: Int,
        reusedFiles: Int,
        previousVersion: Int?,
    ): SubmissionRequest {
        return copy(
            status = RequestStatus.INDEXED_CLEANED,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            conflictingFiles = conflictingFiles,
            deprecatedFiles = deprecatedFiles,
            reusedFiles = reusedFiles,
            previousVersion = previousVersion,
        )
    }
}

/**
 * Retrieves the expected action to be performed when submission request is the given status.
 */
val RequestStatus.action: String
    get() {
        return when (this) {
            REQUESTED -> "Indexing"
            RequestStatus.INDEXED -> "Loading"
            RequestStatus.LOADED -> "Indexing Files to Clean"
            RequestStatus.INDEXED_CLEANED -> "Cleaning"
            RequestStatus.CLEANED -> "Copy Files"
            RequestStatus.FILES_COPIED -> "Release Files"
            RequestStatus.CHECK_RELEASED -> "Save Submission"
            RequestStatus.PERSISTED -> "Submission Post Processing"
            else -> error("Invalid state $this")
        }
    }
