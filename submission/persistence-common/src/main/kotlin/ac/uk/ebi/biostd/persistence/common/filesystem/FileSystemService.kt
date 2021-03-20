package ac.uk.ebi.biostd.persistence.common.filesystem

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FileSystemService(
    private val filesService: FilesService,
    private val ftpLinksService: FtpFilesService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        logger.info { "processing submission ${submission.accNo} files in mode $mode" }
        ftpLinksService.cleanFtpFolder(submission.relPath)
        val processedSubmission = filesService.persistSubmissionFiles(submission, mode)
        if (submission.released) ftpLinksService.createFtpFolder(submission.relPath)
        return processedSubmission
    }
}
