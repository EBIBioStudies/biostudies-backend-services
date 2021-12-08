package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.transaction.annotation.Transactional

internal open class SqlSubmissionRequestService(
    private val submissionService: SubmissionSqlPersistenceService,
    private val lockExecutor: LockExecutor
) : SubmissionRequestService {

    @Transactional(readOnly = true)
    override fun saveSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (sub, _, _, accNo) = saveRequest
        return lockExecutor.executeLocking(accNo) { submissionService.saveSubmissionRequest(sub) }
    }

    @Transactional(readOnly = true)
    override fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (_, _, _, accNo) = saveRequest
        return lockExecutor.executeLocking(accNo) { submissionService.processSubmission(saveRequest) }
    }
}
