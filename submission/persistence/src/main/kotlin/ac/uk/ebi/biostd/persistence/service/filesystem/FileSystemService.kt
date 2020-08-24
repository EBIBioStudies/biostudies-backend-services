package ac.uk.ebi.biostd.persistence.service.filesystem

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FileSystemService(
    private val filesService: FilesService,
    private val ftpLinksService: FtpFilesService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode) {
        logger.info { "processing submission ${submission.accNo} files in mode $mode" }
        ftpLinksService.cleanFtpFolder(submission.relPath)
        filesService.persistSubmissionFiles(submission, mode)
        if (submission.released) ftpLinksService.createFtpFolder(submission.relPath)
    }
}
