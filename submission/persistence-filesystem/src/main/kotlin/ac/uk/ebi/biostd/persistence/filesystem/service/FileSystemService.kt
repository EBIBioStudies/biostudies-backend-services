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
        val (submission, mode) = request
        val accNo = submission.accNo
        val owner = submission.owner

        logger.info { "$accNo $owner Processing files of submission $accNo in mode $mode" }

        val processedSubmission = filesService.persistSubmissionFiles(request)
        val finalSub = pageTabService.generatePageTab(processedSubmission)

        logger.info { "$accNo $owner Finished processing files of submission $accNo in mode $mode" }

        return finalSub
    }

    fun releaseSubmissionFiles(accNo: String, owner: String, relPath: String) {
        logger.info { "$accNo $owner Releasing files of submission $accNo" }

        ftpService.releaseSubmissionFiles(accNo, owner, relPath)

        logger.info { "$accNo $owner Finished releasing files of submission $accNo" }
    }

    fun unpublishSubmissionFiles(accNo: String, owner: String, relPath: String) {
        logger.info { "$accNo $owner Un-publishing files of submission $accNo" }

        ftpService.unpublishSubmissionFiles(accNo, owner, relPath)

        logger.info { "$accNo $owner Finished un-publishing files of submission $accNo" }
    }
}
