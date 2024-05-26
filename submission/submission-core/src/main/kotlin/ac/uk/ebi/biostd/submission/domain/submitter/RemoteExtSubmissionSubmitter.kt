package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec.Companion.SIXTEEN_GB

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class RemoteExtSubmissionSubmitter(
    private val clusterClient: ClusterClient,
    private val submissionTaskProperties: SubmissionTaskProperties,
) : ExtSubmissionSubmitter {
    override suspend fun createRequest(rqt: ExtSubmitRequest): Pair<String, Int> {
        TODO("Remote execution not required")
    }

    override suspend fun indexRequest(
        accNo: String,
        version: Int,
    ) {
        TODO("Remote execution not required")
    }

    override suspend fun loadRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.LOAD)
    }

    override suspend fun indexToCleanRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.INDEX_TO_CLEAN)
    }

    override suspend fun cleanRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.CLEAN)
    }

    override suspend fun processRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.COPY)
    }

    override suspend fun checkReleased(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.CHECK_RELEASED)
    }

    override suspend fun saveRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        TODO("Remote execution not required")
    }

    override suspend fun finalizeRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        TODO("Remote execution not required")
    }

    override suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        TODO("Remote execution not required")
    }

    private suspend fun executeRemotely(
        accNo: String,
        version: Int,
        mode: Mode,
    ) = withContext(Dispatchers.IO) {
        val command =
            buildString {
                appendSpaced(submissionTaskProperties.javaLocation)
                appendSpaced("-jar")
                appendSpaced(submissionTaskProperties.jarLocation)
                appendSpaced("--spring.config.location=${submissionTaskProperties.configFileLocation}")
                appendSpaced("--accNo=$accNo")
                appendSpaced("--version=$version")
                appendSpaced("--mode=${mode.name}")
            }

        val job = clusterClient.triggerJobAsync(JobSpec(cores = 8, ram = SIXTEEN_GB, DataMoverQueue, command))
        job.fold(
            { logger.info { "$accNo Triggered submission task $mode. Job Id: ${it.id}, Logs: ${it.logsPath}" } },
            { throw it },
        )
    }

    private fun StringBuilder.appendSpaced(value: String) {
        append(value)
        append(" ")
    }
}
