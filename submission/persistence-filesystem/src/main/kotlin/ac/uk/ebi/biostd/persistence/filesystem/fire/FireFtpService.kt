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
        if (submission.released) publishFiles(submission)
    }

    override fun generateFtpLinks(accNo: String) {
        publishFiles(submissionQueryService.getExtByAccNo(accNo))
    }

    private fun publishFiles(submission: ExtSubmission) =
        submission
            .allFiles
            .map { it as FireFile }
            .forEach { fireWebClient.publish(it.fireId) }
}
