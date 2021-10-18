package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FileSystemService(
    private val ftpService: FtpService,
    private val filesService: FilesService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        val accNo = submission.accNo
        val owner = submission.owner

        logger.info { "$accNo $owner Processing files of submission $accNo in mode $mode" }

        val processedSubmission = filesService.persistSubmissionFiles(submission, mode)
        ftpService.processSubmissionFiles(submission)

        logger.info { "$accNo $owner Finished processing files of submission $accNo in mode $mode" }

        return processedSubmission
    }
}
