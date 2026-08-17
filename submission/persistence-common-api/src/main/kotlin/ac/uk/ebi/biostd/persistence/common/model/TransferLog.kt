package ac.uk.ebi.biostd.persistence.common.model

import java.time.Instant

data class TransferLog(
    val timestamp: Instant,
    val user: String,
    val sourceEmail: String,
    val targetEmail: String,
    val operation: TransferOperation,
)

enum class TransferOperation {
    TRANSFER,
    EMAIL_UPDATE,
}
