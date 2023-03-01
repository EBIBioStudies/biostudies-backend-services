package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionRequestLoader(
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    /**
     * Calculate md5 and size for every file in submission request.
     */
    fun loadRequest(accNo: String, version: Int) {
        val request = requestService.getIndexedRequest(accNo, version)
        val sub = request.submission

        logger.info { "${sub.accNo} ${sub.owner} Started loading submission files" }

        loadSubmissionFiles(accNo, version, sub, request.currentIndex)
        requestService.saveSubmissionRequest(request.withNewStatus(LOADED))

        logger.info { "${sub.accNo} ${sub.owner} Finished loading submission files" }
    }

    private fun loadSubmissionFiles(accNo: String, version: Int, sub: ExtSubmission, startingAt: Int) {
        filesRequestService
            .getSubmissionRequestFiles(accNo, sub.version, startingAt)
            .forEach {
                when (val file = it.file) {
                    is FireFile -> requestService.updateRqtIndex(accNo, version, it.index)
                    is NfsFile -> requestService.updateRqtIndex(it, loadAttributes(file))
                }
                logger.info { "$accNo ${sub.owner} Finished loading file ${it.index}, path='${it.path}'" }
            }
    }

    private fun loadAttributes(file: ExtFile): ExtFile = when (file) {
        is FireFile -> file
        is NfsFile -> file.copy(md5 = file.file.md5(), size = file.file.size())
    }
}
