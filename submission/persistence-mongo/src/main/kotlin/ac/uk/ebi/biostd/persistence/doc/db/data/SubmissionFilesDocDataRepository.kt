package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionFile
import kotlinx.coroutines.flow.Flow

class SubmissionFilesDocDataRepository(
    private val submissionDocFileRepository: SubmissionDocFileRepository,
) : SubmissionDocFileRepository by submissionDocFileRepository {
    suspend fun findByAccNoAndVersion(
        accNo: String,
        version: Int,
    ): Flow<DocSubmissionFile> = submissionDocFileRepository.findAllBySubmissionAccNoAndSubmissionVersion(accNo, version)
}
