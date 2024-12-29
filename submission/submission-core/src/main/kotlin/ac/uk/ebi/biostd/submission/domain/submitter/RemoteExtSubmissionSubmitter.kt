package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService.Companion.SYNC_SUBMIT_TIMEOUT
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.SubmissionId
import mu.KotlinLogging
import java.time.Duration.ofMinutes

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class RemoteExtSubmissionSubmitter(
    private val remoteSubmitterExecutor: RemoteSubmitterExecutor,
    private val submissionQueryService: ExtSubmissionQueryService,
    private val requestService: SubmissionRequestPersistenceService,
) : ExtSubmissionSubmitter {
    override suspend fun createRqt(rqt: ExtSubmitRequest): Pair<String, Int> {
        TODO("Not yet implemented")
    }

    override suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        val args = listOf(ExecutionArg("submissions[0].accNo", accNo), ExecutionArg("submissions[0].version", version))
        remoteSubmitterExecutor.executeRemotely(args, Mode.HANDLE_REQUEST)

        waitUntil(timeout = ofMinutes(SYNC_SUBMIT_TIMEOUT)) { requestService.isRequestCompleted(accNo, version) }
        return submissionQueryService.getExtendedSubmission(accNo)
    }

    override suspend fun handleRequestAsync(
        accNo: String,
        version: Int,
    ) {
        val args = listOf(ExecutionArg("submissions[0].accNo", accNo), ExecutionArg("submissions[0].version", version))
        remoteSubmitterExecutor.executeRemotely(args, Mode.HANDLE_REQUEST)
    }

    override suspend fun handleManyAsync(submissions: List<SubmissionId>) {
        val args = asExecutionArgs(submissions)
        remoteSubmitterExecutor.executeRemotely(args, Mode.HANDLE_REQUEST)
    }

    override suspend fun refreshAllStats() {
        remoteSubmitterExecutor.executeRemotely(emptyList(), Mode.CALCULATE_ALL_STATS)
    }

    private fun asExecutionArgs(submissions: List<SubmissionId>): List<ExecutionArg> =
        buildList<ExecutionArg> {
            submissions.forEachIndexed { index, submissionId ->
                add(ExecutionArg("submissions[$index].accNo", submissionId.accNo))
                add(ExecutionArg("submissions[$index].version", submissionId.version))
            }
        }
}
