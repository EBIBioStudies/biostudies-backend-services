package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.transaction.annotation.Transactional

internal open class SqlSubmissionRequestService(
    private val submissionService: SubmissionSqlPersistenceService,
    private val lockExecutor: LockExecutor
) : SubmissionRequestService {

    @Transactional(readOnly = true)
    override fun saveSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int> {
        val saved = lockExecutor.executeLocking(rqt.submission.accNo) { submissionService.saveSubmissionRequest(rqt) }
        return saved.accNo to saved.version
    }

    @Transactional(readOnly = true)
    override fun processSubmissionRequest(saveRequest: SubmissionRequest): ExtSubmission =
        lockExecutor.executeLocking(saveRequest.submission.accNo) { submissionService.processSubmission(saveRequest) }
}
