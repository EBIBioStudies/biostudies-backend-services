package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import org.springframework.transaction.annotation.Transactional

@Suppress("TooManyFunctions")
internal open class SqlSubmissionRequestService(
    private val submissionService: SubmissionSqlPersistenceService,
    private val lockExecutor: LockExecutor
) : SubmissionRequestService {

    /**
     * Register the submission in the persistence state and latter process it. Note that both operations are executed
     * under db lock to guarantee single submission is saved and process at time.
     */
    @Transactional(readOnly = true)
    override fun saveAndProcessSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val extended = saveSubmissionRequest(saveRequest)
        return processSubmission(SaveSubmissionRequest(extended, saveRequest.fileMode))
    }

    @Transactional(readOnly = true)
    override fun saveSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (sub, _, accNo) = saveRequest
        return lockExecutor.executeLocking(accNo) { submissionService.saveSubmissionRequest(sub) }
    }

    @Transactional(readOnly = true)
    override fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (sub, mode, accNo) = saveRequest
        return lockExecutor.executeLocking(accNo) { submissionService.processSubmission(sub, mode) }
    }

    @Transactional
    override fun refreshSubmission(submission: ExtSubmission) {
        saveAndProcessSubmissionRequest(
            SaveSubmissionRequest(submission.copy(version = submission.version + 1), FileMode.MOVE))
    }
}
