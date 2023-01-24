package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionRequestProcessor(
    private val storageService: FileStorageService,
    private val queryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    /**
     * Process the current submission files. Note that [ExtSubmission] returned does not include file list files.
     */
    fun processRequest(accNo: String, version: Int) {
        val request = requestService.getCleanedRequest(accNo, version)
        val new = request.submission
        val current = queryService.findExtByAccNo(accNo, includeFileListFiles = true)

        logger.info { "$accNo ${new.owner} Started persisting submission files on ${new.storageMode}" }

        persistSubmissionFiles(new, request.currentIndex)
        storageService.postProcessSubmissionFiles(new, current)
        requestService.saveSubmissionRequest(request.withNewStatus(FILES_COPIED))
        logger.info { "$accNo ${new.owner} Finished persisting submission files on ${new.storageMode}" }
    }

    private fun persistSubmissionFiles(sub: ExtSubmission, startingAt: Int) {
        filesRequestService
            .getSubmissionRequestFiles(sub.accNo, sub.version, startingAt)
            .map {
                logger.info { "${sub.accNo} ${sub.owner} Started persisting file ${it.index}, path='${it.path}'" }
                it.copy(file = storageService.persistSubmissionFile(sub, it.file))
            }
            .forEach {
                requestService.updateRequestFile(it)
                logger.info { "${sub.accNo} ${sub.owner} Finished persisting file ${it.index}, path='${it.path}'" }
            }
    }
}
