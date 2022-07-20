package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionReleaser(
    private val ftpService: FtpService,
    private val submissionPersistenceService: SubmissionPersistenceService,
) {

    fun releaseSubmission(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Releasing submission ${sub.accNo}" }
        submissionPersistenceService.setAsReleased(sub.accNo)
        ftpService.releaseSubmissionFiles(sub)
        logger.info { "${sub.accNo} ${sub.owner} released submission ${sub.accNo}" }
    }
}
