package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.TaskHostProperties

class ExtendedSubmissionSubmitter(
    private val localExtSubmissionSubmitter: LocalExtSubmissionSubmitter,
    private val remoteExtSubmissionSubmitter: RemoteExtSubmissionSubmitter,
    private val hostProperties: TaskHostProperties,
) : ExtSubmissionSubmitter by localExtSubmissionSubmitter {
    override suspend fun loadRequest(accNo: String, version: Int) {
        when (hostProperties.enableTaskMode) {
            true -> remoteExtSubmissionSubmitter.loadRequest(accNo, version)
            else -> localExtSubmissionSubmitter.loadRequest(accNo, version)
        }
    }

    override suspend fun processRequest(accNo: String, version: Int) {
        when (hostProperties.enableTaskMode) {
            true -> remoteExtSubmissionSubmitter.processRequest(accNo, version)
            else -> localExtSubmissionSubmitter.processRequest(accNo, version)
        }
    }
}
