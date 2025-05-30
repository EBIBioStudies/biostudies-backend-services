package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.model.constants.ProcessingStatus
import java.time.OffsetDateTime

/**
 * Submission basic projection. Contains only submission attributes (no related entities).
 */
data class BasicSubmission(
    val accNo: String,
    val released: Boolean,
    val title: String?,
    val releaseTime: OffsetDateTime?,
    val modificationTime: OffsetDateTime,
    var status: ProcessingStatus,
    val owner: String,
    val errors: List<String>,
)
