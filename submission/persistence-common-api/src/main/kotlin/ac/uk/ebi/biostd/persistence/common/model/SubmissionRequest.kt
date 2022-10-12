package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtSubmission
import java.time.OffsetDateTime

data class SubmissionRequest(
    val submission: ExtSubmission,
    val draftKey: String?,
    val status: RequestStatus,
    val totalFiles: Int,
    val currentIndex: Int,
    val modificationTime: OffsetDateTime,
)

enum class RequestStatus {
    REQUESTED,
    LOADED,
    CLEANED,
    FILES_COPIED,
    PROCESSED;

    companion object {
        val PROCESSING: Set<RequestStatus> = setOf(REQUESTED, LOADED, CLEANED, FILES_COPIED)
    }
}
