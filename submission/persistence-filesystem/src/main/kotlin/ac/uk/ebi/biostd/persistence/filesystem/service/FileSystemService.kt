package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FileSystemService(
    private val ftpService: FtpService,
    private val filesService: FilesService,
    private val pageTabService: PageTabService,
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        logger.info { "processing submission ${submission.accNo} files in mode $mode" }

        val processedSubmission = filesService.persistSubmissionFiles(submission, mode)
        pageTabService.generatePageTab(processedSubmission)
        ftpService.processSubmissionFiles(submission)

        return processedSubmission
    }
}
