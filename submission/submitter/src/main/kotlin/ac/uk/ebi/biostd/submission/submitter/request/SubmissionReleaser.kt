package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionReleaser(
    private val fileStorageService: FileStorageService,
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
) {

    /**
     * Check the release status of the submission and release it if released flag is true.
     */
    fun checkReleased(accNo: String, version: Int): ExtSubmission {
        val sub = queryService.getExtByAccNoAndVersion(accNo, version, includeFileListFiles = true)
        if (sub.released) releaseSubmission(sub)
        persistenceService.updateRequestStatus(sub.accNo, sub.version, PROCESSED)
        return sub
    }

    /**
     * Release the given submission by changing record status database and publishing files.
     */
    fun releaseSubmission(accNo: String) {
        val submission = queryService.getExtByAccNo(accNo, includeFileListFiles = true)
        releaseSubmission(submission)
    }

    /**
     * Generates/refresh FTP status for a given submission.
     */
    fun generateFtp(accNo: String) {
        val sub = queryService.getExtByAccNo(accNo, includeFileListFiles = true)
        fileStorageService.releaseSubmissionFiles(sub)
    }

    private fun releaseSubmission(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Releasing submission ${sub.accNo}" }
        persistenceService.setAsReleased(sub.accNo)
        fileStorageService.releaseSubmissionFiles(sub)
        logger.info { "${sub.accNo} ${sub.owner} released submission ${sub.accNo}" }
    }
}
