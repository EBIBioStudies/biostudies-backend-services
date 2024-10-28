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
    val fileChanges: SubmissionRequestFileChanges,
    val currentIndex: Int,
    val modificationTime: OffsetDateTime,
    val silentMode: Boolean,
    val processAll: Boolean,
    val previousVersion: Int?,
) {
    constructor(
        submission: ExtSubmission,
        notifyTo: String,
        silentMode: Boolean,
        processAll: Boolean,
        draftKey: String? = null,
    ) : this(
        submission,
        draftKey,
        notifyTo,
        status = REQUESTED,
        totalFiles = 0,
        fileChanges = SubmissionRequestFileChanges(),
        currentIndex = 0,
        previousVersion = null,
        silentMode = silentMode,
        processAll = processAll,
        modificationTime = OffsetDateTime.now(),
    )

    /**
     * Update request by setting new status, resetting current Index and updating modification date.
     */
    fun withNewStatus(status: RequestStatus): SubmissionRequest =
        copy(
            status = status,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
        )

    /**
     * Create a Submission Request after indexing stage setting total files field.
     */
    fun indexed(totalFiles: Int): SubmissionRequest =
        copy(
            status = RequestStatus.INDEXED,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            totalFiles = totalFiles,
        )

    /**
     * Create a Submission Request after clean indexing stage setting conflicted, deprecated files and previous version
     * fields.
     */
    fun cleanIndexed(
        fileChanges: SubmissionRequestFileChanges,
        previousVersion: Int?,
    ): SubmissionRequest =
        copy(
            status = RequestStatus.INDEXED_CLEANED,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            fileChanges = fileChanges,
            previousVersion = previousVersion,
        )
}

data class SubmissionRequestFileChanges(
    val reusedFiles: Int = 0,
    val deprecatedFiles: Int = 0,
    val deprecatedPageTab: Int = 0,
    val conflictingFiles: Int = 0,
    val conflictingPageTab: Int = 0,
)

/**
 * Retrieves the expected action to be performed when submission request is the given status.
 */
val RequestStatus.action: String
    get() {
        return when (this) {
            REQUESTED -> "Indexing"
            RequestStatus.INDEXED -> "Loading"
            RequestStatus.LOADED -> "Indexing Files to Clean"
            RequestStatus.INDEXED_CLEANED -> "Validating"
            RequestStatus.VALIDATED -> "Cleaning"
            RequestStatus.CLEANED -> "Copying Files"
            RequestStatus.FILES_COPIED -> "Releasing Files"
            RequestStatus.CHECK_RELEASED -> "Saving Submission"
            RequestStatus.PERSISTED -> "Submission Post Processing"
            else -> error("Invalid state $this")
        }
    }
