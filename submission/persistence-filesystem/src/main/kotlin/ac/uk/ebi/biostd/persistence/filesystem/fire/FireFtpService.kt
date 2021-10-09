package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.allFiles
import uk.ac.ebi.fire.client.integration.web.FireWebClient

class FireFtpService(
    private val fireWebClient: FireWebClient,
    private val submissionQueryService: SubmissionQueryService
) : FtpService {
    override fun processSubmissionFiles(submission: ExtSubmission) {
        cleanFtpFolder(submission.relPath)
        if (submission.released) publishFiles(submission)
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
