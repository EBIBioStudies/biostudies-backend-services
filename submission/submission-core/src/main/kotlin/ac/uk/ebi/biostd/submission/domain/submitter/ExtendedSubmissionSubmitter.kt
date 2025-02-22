package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.SubmissionId

@Suppress("TooManyFunctions")
class ExtendedSubmissionSubmitter(
    private val localExtSubmissionSubmitter: LocalExtSubmissionSubmitter,
    private val remoteExtSubmissionSubmitter: RemoteExtSubmissionSubmitter,
    private val submissionTaskProperties: SubmissionTaskProperties,
) : ExtSubmissionSubmitter {
    override suspend fun createRqt(rqt: ExtSubmitRequest): Pair<String, Int> {
        return localExtSubmissionSubmitter.createRqt(rqt)
    }

    override suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission =
        if (submissionTaskProperties.enabled) {
            remoteExtSubmissionSubmitter.handleRequest(accNo, version)
        } else {
            localExtSubmissionSubmitter.handleRequest(accNo, version)
        }

    override suspend fun handleRequestAsync(
        accNo: String,
        version: Int,
    ) {
        if (submissionTaskProperties.enabled) {
            remoteExtSubmissionSubmitter.handleRequestAsync(accNo, version)
        } else {
            localExtSubmissionSubmitter.handleRequestAsync(accNo, version)
        }
    }

    override suspend fun handleManyAsync(submissions: List<SubmissionId>) {
        if (submissionTaskProperties.enabled) {
            remoteExtSubmissionSubmitter.handleManyAsync(submissions)
        } else {
            localExtSubmissionSubmitter.handleManyAsync(submissions)
        }
    }

    override suspend fun refreshAllStats() {
        if (submissionTaskProperties.enabled) {
            remoteExtSubmissionSubmitter.refreshAllStats()
        } else {
            localExtSubmissionSubmitter.refreshAllStats()
        }
    }
}
