package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleaner(
    private val storageService: FileStorageService,
    private val queryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    fun cleanCurrentVersion(accNo: String, version: Int) {
        val request = requestService.getLoadedRequest(accNo, version)
        val new = request.submission
        val current = queryService.findExtByAccNo(accNo, includeFileListFiles = true)

        logger.info { "${new.accNo} ${new.owner} Started cleaning files of version ${new.version}" }
        storageService.prepareSubmissionFiles(new, current)
        logger.info { "${new.accNo} ${new.owner} Finished cleaning files of version ${new.version}" }

        requestService.saveSubmissionRequest(request.withNewStatus(status = CLEANED))
    }
}
