package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
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

    fun processRequest(saveRequest: SubmissionRequest): ExtSubmission {
        val (sub, fileMode, draftKey) = saveRequest
        logger.info { "${sub.accNo} ${sub.owner} processing request accNo='${sub.accNo}', version='${sub.version}'" }

        if (saveRequest.previousVersion != null) systemService.cleanFolder(saveRequest.previousVersion!!)

        val processingSubmission = systemService.persistSubmissionFiles(FilePersistenceRequest(sub, fileMode))
        val savedSubmission = submissionPersistenceService.saveSubmission(processingSubmission, draftKey)

        submissionPersistenceService.updateRequestAsProcessed(sub.accNo, sub.version)

        if (savedSubmission.released) {
            releaseSubmission(savedSubmission.accNo, savedSubmission.owner, savedSubmission.relPath)
        }

        logger.info { "${sub.accNo} ${sub.owner} processed request accNo='${sub.accNo}', version='${sub.version}'" }
        return savedSubmission
    }

    fun releaseSubmission(accNo: String, owner: String, relPath: String) {
        logger.info { "$accNo $owner Releasing submission $accNo" }

        submissionPersistenceService.setAsReleased(accNo)
        systemService.releaseSubmissionFiles(accNo, owner, relPath)

        logger.info { "$accNo $owner released submission $accNo" }
    }
}
