package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionRequestProcessor(
    private val systemService: FileSystemService,
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
) {
    /**
     * Process the current submission files. Note that [ExtSubmission] returned does not include file list files.
     */
    fun processRequest(accNo: String, version: Int): ExtSubmission {
        val request = queryService.getLoadedRequest(accNo, version)
        val (sub, draftKey) = request

        logger.info { "$accNo ${sub.owner} Copying files accNo='${sub.accNo}', version='$version'" }

        val processed = systemService.persistSubmissionFiles(sub)
        persistenceService.saveSubmission(processed, draftKey)
        persistenceService.saveSubmissionRequest(request.copy(status = FILES_COPIED, submission = processed))

        logger.info { "$accNo ${sub.owner} Finished copying files accNo='$accNo', version='$version'" }

        return processed
    }

    fun cleanCurrentVersion(accNo: String) {
        val sub = queryService.findExtByAccNo(accNo, includeFileListFiles = true)
        if (sub != null) {
            logger.info { "${sub.accNo} ${sub.owner} Started cleaning files of version ${sub.version}" }
            systemService.cleanFolder(sub)
            logger.info { "${sub.accNo} ${sub.owner} Finished cleaning files of version ${sub.version}" }
        }
    }
}
