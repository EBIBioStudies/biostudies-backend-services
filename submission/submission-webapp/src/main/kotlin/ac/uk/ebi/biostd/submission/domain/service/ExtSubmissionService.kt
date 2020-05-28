package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

class ExtSubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val userPrivilegesService: IUserPrivilegesService
) {
    fun getExtendedSubmission(user: String, accNo: String): ExtSubmission {
        validateUser(user)
        return submissionRepository.getExtByAccNo(accNo)
    }

    fun submitExtendedSubmission(user: String, extSubmission: ExtSubmission): ExtSubmission {
        validateUser(user)
        return extSubmission
    }

    private fun validateUser(user: String) =
        require(userPrivilegesService.canSubmitExtended(user)) {
            throw SecurityException("The user $user is not allowed to perform this action")
        }
}
