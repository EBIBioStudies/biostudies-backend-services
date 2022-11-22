package ac.uk.ebi.biostd.persistence.common.model

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
    fun withNewStatus(status: RequestStatus, totalFiles: Int? = null): SubmissionRequest {
        return copy(
            status = status,
            modificationTime = OffsetDateTime.now(),
            currentIndex = 0,
            totalFiles = totalFiles ?: this.totalFiles
        )
    }
}

enum class RequestStatus {
    REQUESTED,
    INDEXED,
    LOADED,
    CLEANED,
    FILES_COPIED,
    PROCESSED;

    companion object {
        val PROCESSING: Set<RequestStatus> = setOf(REQUESTED, LOADED, CLEANED, FILES_COPIED)
    }
}
