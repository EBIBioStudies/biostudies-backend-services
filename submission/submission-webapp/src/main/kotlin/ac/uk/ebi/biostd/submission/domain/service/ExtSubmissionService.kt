package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.repositories.data.SubmissionRepository
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.data.domain.Page

class ExtSubmissionService(
    private val persistenceService: PersistenceService,
    private val submissionRepository: SubmissionRepository,
    private val userPrivilegesService: IUserPrivilegesService
) {
    fun getExtendedSubmission(accNo: String): ExtSubmission = submissionRepository.getExtByAccNo(accNo)

    fun submitExtendedSubmission(user: String, extSubmission: ExtSubmission): ExtSubmission {
        validateUser(user)
        return persistenceService.saveAndProcessSubmissionRequest(SaveSubmissionRequest(extSubmission, COPY))
    }

    fun getExtendedSubmissions(request: ExtPageRequest): Page<ExtSubmission> =
        submissionRepository.getExtendedSubmissions(request.offset, request.limit)

    private fun validateUser(user: String) =
        require(userPrivilegesService.canSubmitExtended(user)) {
            throw SecurityException("The user '$user' is not allowed to perform this action")
        }
}
