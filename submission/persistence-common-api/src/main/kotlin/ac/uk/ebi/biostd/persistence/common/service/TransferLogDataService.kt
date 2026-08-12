package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.TransferLog
import ac.uk.ebi.biostd.persistence.common.model.TransferOperation

interface TransferLogDataService {
    fun logTransfer(
        user: String,
        sourceEmail: String,
        targetEmail: String,
        operation: TransferOperation,
    )

    fun findLatest(
        sourceEmail: String,
        targetEmail: String,
        operation: TransferOperation,
    ): TransferLog?
}
