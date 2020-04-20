package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository

class SubmissionPersistenceService(
    private val submissionRepository: SubmissionRepository,
    private val persistenceContext: PersistenceContext
) : SubmissionRepository by submissionRepository {

    fun refresh(accNo: String) {
        val submission = submissionRepository.getByAccNo(accNo)
        persistenceContext.refreshFileSystem(submission)
    }
}
