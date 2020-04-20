package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository

class SubmissionPersistenceService(
    private val submissionRepository: SubmissionRepository,
    private val persistenceContext: PersistenceContext
) {

    fun refresh(accNo: String) {
        val submission = submissionRepository.getExtByAccNo(accNo)
        persistenceContext.refreshFileSystem(submission)
    }
}
