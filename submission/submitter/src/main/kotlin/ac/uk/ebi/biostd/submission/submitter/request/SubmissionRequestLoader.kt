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
import java.time.OffsetDateTime

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

        loadSubmissionFiles(sub, request.currentIndex)
        val loadedRequest = request.copy(
            status = LOADED,
            currentIndex = 0,
            modificationTime = OffsetDateTime.now(),
        )

        requestService.saveSubmissionRequest(loadedRequest)

        logger.info { "${sub.accNo} ${sub.owner} Finished loading submission files" }
    }

    private fun loadSubmissionFiles(sub: ExtSubmission, startingAt: Int) {
        filesRequestService
            .getSubmissionRequestFiles(sub.accNo, sub.version, startingAt)
            .map {
                logger.info { "${sub.accNo} ${sub.owner} Started loading file ${it.index}, path='${it.path}'" }
                it.copy(file = loadFileAttributes(it.file))
            }
            .forEach {
                filesRequestService.saveSubmissionRequestFile(it)
                requestService.updateRequestIndex(sub.accNo, sub.version, it.index)
                logger.info { "${sub.accNo} ${sub.owner} Finished loading file ${it.index}, path='${it.path}'" }
            }
    }

    private fun loadFileAttributes(file: ExtFile): ExtFile = when (file) {
        is FireFile -> file
        is NfsFile -> file.copy(md5 = file.file.md5(), size = file.file.size())
    }
}
