package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.PmcSubmissionsRepository
import ac.uk.ebi.biostd.persistence.doc.model.PmcDocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.PmcSubmissionStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC

class PmcSubmissionsDocDataRepository(
    private val pmcRepository: PmcSubmissionsRepository,
) : PmcSubmissionsRepository by pmcRepository {
    fun findByStatus(
        status: PmcSubmissionStatus,
        records: Int,
    ): Flow<PmcDocSubmission> = pmcRepository.findByStatus(status, PageRequest.of(0, records, Sort.by(ASC, "_id")))

    suspend fun updateStatus(
        submissionId: List<PmcDocSubmission>,
        status: PmcSubmissionStatus,
    ) {
        pmcRepository.updateStatusByIds(submissionId.map { it.id }, status)
    }
}
