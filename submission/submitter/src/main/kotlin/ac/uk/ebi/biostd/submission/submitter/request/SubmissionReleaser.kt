package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionReleaser(
    private val fileStorageService: FileStorageService,
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
    private val submissionPersistenceService: SubmissionPersistenceService,
) {

    fun releaseSubmission(accNo: String) {
        val sub = submissionPersistenceQueryService.getExtByAccNo(accNo, includeFileListFiles = true)
        logger.info { "${sub.accNo} ${sub.owner} Releasing submission ${sub.accNo}" }
        submissionPersistenceService.setAsReleased(sub.accNo)
        fileStorageService.releaseSubmissionFiles(sub)
        logger.info { "${sub.accNo} ${sub.owner} released submission ${sub.accNo}" }
    }

    fun generateFtpLinks(accNo: String) {
        val sub = submissionPersistenceQueryService.getExtByAccNo(accNo, includeFileListFiles = true)
        fileStorageService.releaseSubmissionFiles(sub)
    }
}