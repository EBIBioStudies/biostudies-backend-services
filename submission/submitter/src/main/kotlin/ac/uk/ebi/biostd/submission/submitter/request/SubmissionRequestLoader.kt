package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
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
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
    private val fileProcessingService: FileProcessingService,
) {

    internal fun loadRequest(accNo: String, version: Int): SubmissionRequest {
        logger.info { "Loading request accNo='$accNo', version='$version'" }
        val rqt = submissionPersistenceQueryService.getPendingRequest(accNo, version)
        val full = processRequest(rqt.submission)
        logger.info { "Finish Loading request accNo='$accNo', version='$version'" }
        return SubmissionRequest(full, rqt.fileMode, rqt.draftKey)
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