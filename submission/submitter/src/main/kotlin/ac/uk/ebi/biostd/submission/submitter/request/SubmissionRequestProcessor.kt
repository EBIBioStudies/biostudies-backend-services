package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.request.ProcessedSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionRequestProcessor(
    private val systemService: FileSystemService,
    private val submissionPersistenceService: SubmissionPersistenceService,
) {

    fun processRequest(saveRequest: ProcessedSubmissionRequest): ExtSubmission {
        val (sub, fileMode, draftKey, previousVersion) = saveRequest
        logger.info { "${sub.accNo} ${sub.owner} processing request accNo='${sub.accNo}', version='${sub.version}'" }

        if (previousVersion != null) {
            logger.info { "${sub.accNo} ${sub.owner} Started cleaning files of version ${previousVersion.version}" }
            systemService.cleanFolder(previousVersion)
            logger.info { "${sub.accNo} ${sub.owner} Finished cleaning files of version ${previousVersion.version}" }
        }

        val processingSubmission = systemService.persistSubmissionFiles(FilePersistenceRequest(sub, fileMode))
        val savedSubmission = submissionPersistenceService.saveSubmission(processingSubmission, draftKey)

        submissionPersistenceService.updateRequestAsProcessed(sub.accNo, sub.version)

        logger.info { "${sub.accNo} ${sub.owner} processed request accNo='${sub.accNo}', version='${sub.version}'" }
        return savedSubmission
    }
}
