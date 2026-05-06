package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.SubmissionId
import mu.KotlinLogging
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class RemoteExtSubmissionSubmitter(
    private val remoteSubmitterExecutor: RemoteSubmitterExecutor,
    private val submissionQueryService: ExtSubmissionQueryService,
    private val requestService: SubmissionRequestPersistenceService,
) : ExtSubmissionSubmitter {
    override suspend fun createRqt(rqt: ExtSubmitRequest): SubmissionId {
        TODO("Not yet implemented")
    }

    override suspend fun handleRequest(
        waitTime: Duration = SYNC_SUBMIT_TIMEOUT.minutes,
        accNo: String,
        version: Int,
        waitTime: Duration,
    ): ExtSubmission {
        val args = listOf(SubmissionId(accNo, version))
        remoteSubmitterExecutor.executeRemotely(asExecutionArgs(args), Mode.HANDLE_REQUEST)

        waitUntil(timeout = waitTime) { requestService.isRequestCompleted(accNo, version) }
        return submissionQueryService.getExtendedSubmission(accNo)
    }

    override suspend fun handleMany(
        submissions: List<SubmissionId>,
        waitTime: Duration,
    ): List<ExtSubmission> {
        if (submissions.isEmpty()) return emptyList()
        val job = remoteSubmitterExecutor.executeRemotely(asExecutionArgs(submissions), Mode.HANDLE_REQUEST)
        remoteSubmitterExecutor.waitForJob(job, waitTime.times(submissions.size))
        return submissions
            .filter { requestService.isRequestCompleted(it.accNo, it.version) }
            .map { submissionQueryService.getExtendedSubmission(it.accNo) }
    }

    override suspend fun handleRequestAsync(
        accNo: String,
        version: Int,
    ) {
        val args = listOf(SubmissionId(accNo, version))
        remoteSubmitterExecutor.executeRemotely(asExecutionArgs(args), Mode.HANDLE_REQUEST)
    }

    override suspend fun handleManyAsync(submissions: List<SubmissionId>) {
        val args = asExecutionArgs(submissions)
        remoteSubmitterExecutor.executeRemotely(args, Mode.HANDLE_REQUEST)
    }

    private fun asExecutionArgs(submissions: List<SubmissionId>): List<ExecutionArg> =
        buildList {
            submissions.forEachIndexed { index, submissionId ->
                add(ExecutionArg("submissions[$index].accNo", submissionId.accNo))
                add(ExecutionArg("submissions[$index].version", submissionId.version))
            }
        }
}
