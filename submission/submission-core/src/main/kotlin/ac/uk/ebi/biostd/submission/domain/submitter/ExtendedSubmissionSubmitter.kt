package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties

class ExtendedSubmissionSubmitter(
    private val localExtSubmissionSubmitter: LocalExtSubmissionSubmitter,
    private val remoteExtSubmissionSubmitter: RemoteExtSubmissionSubmitter,
    private val submissionTaskProperties: SubmissionTaskProperties,
) : ExtSubmissionSubmitter by localExtSubmissionSubmitter {
    override suspend fun loadRequest(accNo: String, version: Int) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.loadRequest(accNo, version)
            else -> localExtSubmissionSubmitter.loadRequest(accNo, version)
        }
    }

    override suspend fun cleanRequest(accNo: String, version: Int) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.cleanRequest(accNo, version)
            else -> localExtSubmissionSubmitter.cleanRequest(accNo, version)
        }
    }

    override suspend fun processRequest(accNo: String, version: Int) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.processRequest(accNo, version)
            else -> localExtSubmissionSubmitter.processRequest(accNo, version)
        }
    }

    override suspend fun checkReleased(accNo: String, version: Int) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.checkReleased(accNo, version)
            else -> localExtSubmissionSubmitter.checkReleased(accNo, version)
        }
    }
}
