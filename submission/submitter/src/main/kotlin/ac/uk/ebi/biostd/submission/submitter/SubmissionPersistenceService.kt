package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.security.integration.model.api.SecurityUser

class SubmissionPersistenceService(
    private val submissionRepository: SubmissionRepository,
    private val persistenceContext: PersistenceContext
) {

    fun refresh(accNo: String, user: SecurityUser) {
        val submission = submissionRepository.getExtByAccNo(accNo)
        persistenceContext.refreshSubmission(submission, user.asUser())
    }
}
