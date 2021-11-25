package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allFiles
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

        cleanFtpFolder(submission)
        if (submission.released) publishFiles(submission)

        logger.info { "$accNo $owner Finished publishing files of submission $accNo over FIRE" }
    }

    override fun generateFtpLinks(accNo: String) {
        val submission = submissionQueryService.getExtByAccNo(accNo)
        cleanFtpFolder(submission)
        publishFiles(submission)
    }

    private fun cleanFtpFolder(submission: ExtSubmission) =
        submission.allFiles
            .filterIsInstance<FireFile>()
            .forEach { unpublishFile(it.fireId) }

    private fun publishFiles(submission: ExtSubmission) =
        submission.allFiles
            .filterIsInstance<FireFile>()
            .forEach { publishFile(it, submission.relPath) }

    private fun publishFile(file: FireFile, relPath: String) {
        fireWebClient.setPath(file.fireId, "$relPath/${file.relPath}")
        fireWebClient.publish(file.fireId)
    }

    // TODO this should be enabled once the problem with the paths is solved
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
