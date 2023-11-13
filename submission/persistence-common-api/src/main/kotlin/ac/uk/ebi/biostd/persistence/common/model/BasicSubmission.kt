package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.model.SubmissionMethod
import java.math.RoundingMode.HALF_UP
import java.time.OffsetDateTime

/**
 * Submission basic projection. Contains only submission attributes (no related entities).
 */
data class BasicSubmission(
    val accNo: String,
    val relPath: String,
    val released: Boolean,
    val secretKey: String,
    val title: String?,
    val version: Int,
    val releaseTime: OffsetDateTime?,
    val modificationTime: OffsetDateTime,
    val creationTime: OffsetDateTime,
    val method: SubmissionMethod?,
    val status: RequestStatus,
    val totalFiles: Int,
    val currentIndex: Int,
    val owner: String
) {
    val completionPercentage: Double
        get() {
            val completion = (currentIndex.toDouble() / totalFiles) * RequestStatus.WEIGHT_CONSTANT
            val percentage = if (RequestStatus.FILE_PROCESSING_STAGES.contains(status)) status.completion + completion
            else status.completion

            return percentage.toBigDecimal().setScale(2, HALF_UP).toDouble()
        }
}
