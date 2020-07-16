package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.integration.FileMode.COPY
import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.SaveRequest
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

class ExtSubmissionService(
    private val persistenceContext: PersistenceContext,
    private val submissionRepository: SubmissionRepository,
    private val userPrivilegesService: IUserPrivilegesService
) {
    fun getExtendedSubmission(accNo: String): ExtSubmission = submissionRepository.getActiveExtByAccNo(accNo)

    fun submitExtendedSubmission(user: String, extSubmission: ExtSubmission): ExtSubmission {
        validateUser(user)
        return persistenceContext.saveSubmission(SaveRequest(extSubmission, COPY))
    }

    private fun validateUser(user: String) =
        require(userPrivilegesService.canSubmitExtended(user)) {
            throw SecurityException("The user '$user' is not allowed to perform this action")
        }
}
