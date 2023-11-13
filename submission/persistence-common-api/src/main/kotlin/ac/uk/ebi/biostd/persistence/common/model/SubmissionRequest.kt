package ac.uk.ebi.biostd.persistence.common.model

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.Companion.WEIGHT_CONSTANT
import ebi.ac.uk.extended.model.ExtSubmission
import java.time.OffsetDateTime

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
     * Optionally total files can be updated.
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
            totalFiles = totalFiles
        )
    }
}

@Suppress("MagicNumber")
enum class RequestStatus(internal val weight: Double) {
    REQUESTED(0.0),
    INDEXED(1.0),
    LOADED(2.0),
    CLEANED(3.0),
    FILES_COPIED(4.0),
    CHECK_RELEASED(5.0),
    PERSISTED(6.0),
    PROCESSED(7.0);

    companion object {
        val PROCESSING_STAGES: Set<RequestStatus> = setOf(
            REQUESTED,
            INDEXED,
            LOADED,
            CLEANED,
            FILES_COPIED,
            CHECK_RELEASED,
            PERSISTED,
        )

        val FILE_PROCESSING_STAGES: Set<RequestStatus> = setOf(
            REQUESTED,
            INDEXED,
            LOADED,
            CLEANED,
            FILES_COPIED,
        )

        const val DEFAULT_FILES = 1
        const val WEIGHT_CONSTANT = (1.0 / 7)
    }
}

val RequestStatus.completion: Double
    get() = weight * WEIGHT_CONSTANT
