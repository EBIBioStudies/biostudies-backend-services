package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService

private val logger = KotlinLogging.logger {}

class SubmissionRequestLoader(
    private val requestService: SubmissionRequestPersistenceService,
    private val fileProcessingService: FileProcessingService,
    private val storageService: FileStorageService,
) {
    /**
     * Calculate md5 and size for every file in submission request.
     */
    fun loadRequest(accNo: String, version: Int): ExtSubmission {
        logger.info { "Started loading request accNo='$accNo', version='$version'" }
        val original = requestService.getPendingRequest(accNo, version)
        val processed = processRequest(original.submission)
        val withTabFiles = storageService.generatePageTab(processed)
        requestService.saveSubmissionRequest(original.copy(status = LOADED, submission = withTabFiles))
        logger.info { "Finished loading request accNo='$accNo', version='$version'" }
        return withTabFiles
    }

    private fun processRequest(sub: ExtSubmission): ExtSubmission =
        fileProcessingService.processFiles(sub) { file, index ->
            logger.debug { "${sub.accNo}, ${sub.version} Loading file $index, path='${file.filePath}'" }
            loadFileAttributes(file)
        }

    private fun loadFileAttributes(file: ExtFile): ExtFile = when (file) {
        is FireFile -> file
        is NfsFile -> file.copy(md5 = file.file.md5(), size = file.file.size())
    }
}
