package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.PmcSubmissionsDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.PmcDocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.PmcSubmissionStatus
import kotlinx.coroutines.flow.Flow

class PmcSubmissionQueryService(
    private val pmcSubmissionsRepository: PmcSubmissionsDocDataRepository,
) {
    fun findByStatus(
        status: PmcSubmissionStatus,
        records: Int,
    ): Flow<PmcDocSubmission> = pmcSubmissionsRepository.findByStatus(status, records)

    suspend fun updateStatus(
        submissionId: List<PmcDocSubmission>,
        status: PmcSubmissionStatus,
    ) {
        pmcSubmissionsRepository.updateStatus(submissionId, status)
    }

    fun findByAccNos(accNos: kotlin.collections.List<String>): Flow<PmcDocSubmission> = pmcSubmissionsRepository.findByAccNoIn(accNos)
}
