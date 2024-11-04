package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService.Companion.SYNC_SUBMIT_TIMEOUT
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec
import java.time.Duration.ofMinutes

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class RemoteExtSubmissionSubmitter(
    private val clusterClient: ClusterClient,
    private val properties: SubmissionTaskProperties,
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
        executeRemotely(accNo, version, Mode.HANDLE_REQUEST)

        waitUntil(timeout = ofMinutes(SYNC_SUBMIT_TIMEOUT)) { requestService.isRequestCompleted(accNo, version) }
        return submissionQueryService.getExtendedSubmission(accNo)
    }

    override suspend fun handleRequestAsync(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.HANDLE_REQUEST)
    }

    private suspend fun executeRemotely(
        accNo: String,
        version: Int,
        mode: Mode,
    ) = withContext(Dispatchers.IO) {
        val command =
            buildString {
                appendSpaced(properties.javaLocation)
                appendSpaced("-jar")
                appendSpaced(properties.jarLocation)
                appendSpaced("--spring.config.location=${properties.configFileLocation}")
                appendSpaced("--accNo=$accNo")
                appendSpaced("--version=$version")
                appendSpaced("--mode=${mode.name}")
            }

        val job =
            clusterClient.triggerJobAsync(
                JobSpec(
                    cores = properties.taskCores,
                    ram = MemorySpec.fromMegaBytes(properties.taskMemoryMgb),
                    minutes = properties.taskMinutes,
                    queue = DataMoverQueue,
                    command = command,
                ),
            )
        job.fold(
            { logger.info { "$accNo Triggered submission task $mode. Job Id: ${it.id}. ${it.logsPath}" } },
            { throw it },
        )
    }

    private fun StringBuilder.appendSpaced(value: String) {
        append(value)
        append(" ")
    }
}
