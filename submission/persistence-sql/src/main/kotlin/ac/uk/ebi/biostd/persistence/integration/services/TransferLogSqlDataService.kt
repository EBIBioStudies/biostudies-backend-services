package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.model.TransferLog
import ac.uk.ebi.biostd.persistence.common.model.TransferOperation
import ac.uk.ebi.biostd.persistence.common.service.TransferLogDataService
import ac.uk.ebi.biostd.persistence.model.DbTransferLog
import ac.uk.ebi.biostd.persistence.repositories.TransferLogDataRepository
import java.time.Instant

internal class TransferLogSqlDataService(
    private val transferLogRepository: TransferLogDataRepository,
) : TransferLogDataService {
    override fun logTransfer(
        user: String,
        sourceEmail: String,
        targetEmail: String,
        operation: TransferOperation,
    ) {
        transferLogRepository.save(DbTransferLog(0, Instant.now(), user, sourceEmail, targetEmail, operation))
    }

    override fun findLatest(
        sourceEmail: String,
        targetEmail: String,
        operation: TransferOperation,
    ): TransferLog? =
        transferLogRepository
            .findFirstBySourceEmailAndTargetEmailAndOperationOrderByTimestampDesc(sourceEmail, targetEmail, operation)
            ?.asTransferLog()
}
