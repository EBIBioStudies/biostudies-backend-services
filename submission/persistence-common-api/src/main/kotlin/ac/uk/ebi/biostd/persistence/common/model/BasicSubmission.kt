package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
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
    val status: ProcessingStatus,
    val completionPercentage: Double,
    val owner: String
)
