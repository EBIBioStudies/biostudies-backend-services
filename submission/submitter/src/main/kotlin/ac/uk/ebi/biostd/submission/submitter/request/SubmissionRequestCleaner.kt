package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.StorageService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleaner(
    private val storageService: StorageService,
    private val queryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    fun cleanCurrentVersion(accNo: String, version: Int) {
        val request = requestService.getLoadedRequest(accNo, version)
        val sub = queryService.findExtByAccNo(accNo, includeFileListFiles = true)

        if (sub != null) {
            logger.info { "${sub.accNo} ${sub.owner} Started cleaning files of version ${sub.version}" }
            storageService.cleanSubmissionFiles(sub, request.submission)
            logger.info { "${sub.accNo} ${sub.owner} Finished cleaning files of version ${sub.version}" }
        }

        requestService.saveSubmissionRequest(request.withStatus(status = CLEANED))
    }
}
