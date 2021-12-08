package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allSectionsFiles
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient

private val logger = KotlinLogging.logger {}

class FireFtpService(
    private val fireWebClient: FireWebClient,
    private val submissionQueryService: SubmissionQueryService
) : FtpService {
    override fun processSubmissionFiles(submission: ExtSubmission) {
        val accNo = submission.accNo
        val owner = submission.owner

        logger.info { "$accNo $owner Publishing files of submission $accNo over FIRE" }

        cleanFtpFolder(submission.relPath)
        if (submission.released) publishFiles(submission)

        logger.info { "$accNo $owner Finished publishing files of submission $accNo over FIRE" }
    }

    // TODO the referenced files should be retrieved from the database for this endpoint
    // TODO fileList.flatMap { submissionQueryService.getReferencedFiles(sub.accNo, it.fileName) + it.pageTabFiles }
    override fun generateFtpLinks(accNo: String) {
        val submission = submissionQueryService.getExtByAccNo(accNo)
        cleanFtpFolder(submission.relPath)
        publishFiles(submission)
    }

    private fun publishFiles(submission: ExtSubmission) =
        allFiles(submission)
            .filterIsInstance<FireFile>()
            .forEach { publishFile(it, submission.relPath) }

    private fun allFiles(sub: ExtSubmission): Sequence<ExtFile> =
        allFileListFiles(sub).plus(sub.allSectionsFiles).plus(sub.pageTabFiles)

    /**
     * Returns all file list files. Note that sequence is used instead regular iterable to avoid loading all submission
     * files before start processing.
     */
    private fun allFileListFiles(extSubmission: ExtSubmission): Sequence<ExtFile> =
        extSubmission
            .allFileList
            .flatMap { it.files + it.pageTabFiles }
            .asSequence()

    private fun publishFile(file: FireFile, relPath: String) {
        fireWebClient.setPath(file.fireId, "$relPath/${file.relPath}")
        fireWebClient.publish(file.fireId)
    }

    private fun cleanFtpFolder(relPath: String) {
        fireWebClient
            .findAllInPath(relPath)
            .forEach { unpublishFile(it.fireOid) }
    }

    private fun unpublishFile(fireId: String) {
        fireWebClient.unpublish(fireId)
        fireWebClient.unsetPath(fireId)
    }
}
