package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.INVALID
import ebi.ac.uk.model.RequestStatus.REQUESTED
import java.io.File
import java.time.OffsetDateTime

data class SubmissionRequestStatusChange(
    val status: String,
)

data class SubmissionRequestFileChanges(
    val reusedFiles: Int = 0,
    val deprecatedFiles: Int = 0,
    val deprecatedPageTab: Int = 0,
    val conflictingFiles: Int = 0,
    val conflictingPageTab: Int = 0,
)

data class SubmissionRequestProcessing(
    val submission: ExtSubmission,
    val notifyTo: String,
    val totalFiles: Int,
    val fileChanges: SubmissionRequestFileChanges,
    val currentIndex: Int,
    val silentMode: Boolean,
    val singleJobMode: Boolean,
    val previousVersion: Int?,
    val statusChanges: List<SubmissionRequestStatusChange>,
) {
    constructor(
        submission: ExtSubmission,
        previousVersion: Int?,
        notifyTo: String,
        silentMode: Boolean,
        singleJobMode: Boolean,
    ) : this(
        submission,
        notifyTo,
        totalFiles = 0,
        fileChanges = SubmissionRequestFileChanges(),
        currentIndex = 0,
        previousVersion = previousVersion,
        silentMode = silentMode,
        singleJobMode = singleJobMode,
        statusChanges = emptyList(),
    )
}

data class SubmissionRequest(
    val accNo: String,
    val version: Int,
    val owner: String,
    val draft: String? = null,
    val status: RequestStatus,
    val modificationTime: OffsetDateTime,
    val creationTime: OffsetDateTime,
    val newSubmission: Boolean,
    val files: List<File> = emptyList(),
    val preferredSources: List<PreferredSource> = emptyList(),
    val onBehalfUser: String? = null,
    val errors: List<String> = emptyList(),
    val process: SubmissionRequestProcessing? = null,
) {
    constructor(
        accNo: String,
        version: Int,
        owner: String,
        submission: ExtSubmission,
        notifyTo: String,
        silentMode: Boolean,
        singleJobMode: Boolean,
        files: List<File>,
        preferredSources: List<PreferredSource>,
        onBehalfUser: String?,
        newSubmission: Boolean,
        previousVersion: Int?,
    ) : this(
        accNo,
        version,
        owner,
        files = files,
        preferredSources = preferredSources,
        onBehalfUser = onBehalfUser,
        newSubmission = newSubmission,
        status = REQUESTED,
        creationTime = OffsetDateTime.now(),
        modificationTime = OffsetDateTime.now(),
        process = SubmissionRequestProcessing(submission, previousVersion, notifyTo, silentMode, singleJobMode),
    )

    /**
     * Update request by setting new status, resetting current Index and updating modification date.
     */
    fun withNewStatus(status: RequestStatus): SubmissionRequest =
        copy(
            status = status,
            modificationTime = OffsetDateTime.now(),
            process = process?.copy(currentIndex = 0),
        )

    fun withErrors(errors: List<String>): SubmissionRequest = copy(errors = errors, status = INVALID)

    /**
     * Update request by setting new status, resetting current Index, submission body and updating modification date.
     */
    fun withNewStatus(
        status: RequestStatus,
        submission: ExtSubmission,
    ): SubmissionRequest =
        copy(
            status = status,
            modificationTime = OffsetDateTime.now(),
            process = process?.copy(currentIndex = 0, submission = submission),
        )

    /**
     * Create a Submission Request after indexing stage setting total files field.
     */
    fun indexed(totalFiles: Int): SubmissionRequest =
        copy(
            status = RequestStatus.INDEXED,
            modificationTime = OffsetDateTime.now(),
            process = process?.copy(currentIndex = 0, totalFiles = totalFiles),
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
            process = process?.copy(currentIndex = 0, fileChanges = fileChanges, previousVersion = previousVersion),
        )
}

/**
 * Retrieves the expected action to be performed when submission request is the given status.
 */
val RequestStatus.action: String
    get() {
        return when (this) {
            REQUESTED -> "Checking Files"
            RequestStatus.FILES_VALIDATED -> "Indexing"
            RequestStatus.INDEXED -> "Loading"
            RequestStatus.LOADED -> "Indexing Files to Clean"
            RequestStatus.INDEXED_CLEANED -> "Validating"
            RequestStatus.VALIDATED -> "Cleaning"
            RequestStatus.CLEANED -> "Copying Files"
            RequestStatus.FILES_COPIED -> "Releasing Files"
            RequestStatus.CHECK_RELEASED -> "Saving Submission"
            RequestStatus.PERSISTED -> "Cleaning Previous Version Files"
            RequestStatus.PROCESSED -> "Submission Post Processing"
            else -> error("Invalid state $this")
        }
    }
