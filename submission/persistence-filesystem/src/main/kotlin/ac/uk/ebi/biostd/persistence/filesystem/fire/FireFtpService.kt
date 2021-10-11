package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.allFiles
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient

private val logger = KotlinLogging.logger {}

class FireFtpService(
    private val fireWebClient: FireWebClient,
    private val submissionQueryService: SubmissionQueryService
) : FtpService {
    override fun processSubmissionFiles(submission: ExtSubmission) {
        val accNo = submission.accNo
        val submitter = submission.submitter

        logger.info { "$accNo $submitter Publishing files of submission $accNo over FIRE" }

        cleanFtpFolder(submission.relPath)
        if (submission.released) publishFiles(submission)

        logger.info { "$accNo $submitter Finished publishing files of submission $accNo over FIRE" }
    }

    override fun generateFtpLinks(accNo: String) {
        val submission = submissionQueryService.getExtByAccNo(accNo)
        cleanFtpFolder(submission.relPath)
        publishFiles(submission)
    }

    private fun publishFiles(submission: ExtSubmission) =
        submission
            .allFiles
            .filterIsInstance<FireFile>()
            .forEach { publishFile(it, submission.relPath) }

    private fun publishFile(file: FireFile, relPath: String) {
        fireWebClient.setPath(file.fireId, "$relPath/${file.fileName}")
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
