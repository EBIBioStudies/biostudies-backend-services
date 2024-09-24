package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class RemoteExtSubmissionSubmitter(
    private val clusterClient: ClusterClient,
    private val properties: SubmissionTaskProperties,
) {
    suspend fun indexRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.INDEX)
    }

    suspend fun loadRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.LOAD)
    }

    suspend fun indexToCleanRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.INDEX_TO_CLEAN)
    }

    suspend fun validateRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.VALIDATE)
    }

    suspend fun cleanRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.CLEAN)
    }

    suspend fun processRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.COPY)
    }

    suspend fun checkReleased(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.CHECK_RELEASED)
    }

    suspend fun saveRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.SAVE)
    }

    suspend fun finalizeRequest(
        accNo: String,
        version: Int,
    ) {
        executeRemotely(accNo, version, Mode.FINALIZE)
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
                    queue = DataMoverQueue,
                    command = command,
                ),
            )
        job.fold(
            { logger.info { "$accNo Triggered submission task $mode. Job Id: ${it.id}." } },
            { throw it },
        )
    }

    private fun StringBuilder.appendSpaced(value: String) {
        append(value)
        append(" ")
    }
}
