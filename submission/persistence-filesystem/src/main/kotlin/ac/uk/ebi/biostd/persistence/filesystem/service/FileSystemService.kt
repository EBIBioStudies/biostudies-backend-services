package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FileSystemService(
    private val ftpService: FtpService,
    private val filesService: FilesService,
    private val pageTabService: PageTabService,
) {
    fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        val (submission, mode, _) = request
        logger.info { "processing submission ${submission.accNo} files in mode $mode" }

        val processedSubmission = filesService.persistSubmissionFiles(request)
        val finalSub = pageTabService.generatePageTab(processedSubmission)
        ftpService.processSubmissionFiles(finalSub)
        return finalSub
    }
}
