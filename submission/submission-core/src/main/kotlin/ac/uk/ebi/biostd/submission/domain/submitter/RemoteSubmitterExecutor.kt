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

class RemoteSubmitterExecutor(
    private val properties: SubmissionTaskProperties,
    private val clusterClient: ClusterClient,
) {
    suspend fun executeRemotely(
        args: List<ExecutionArg>,
        mode: Mode,
    ): Unit =
        withContext(Dispatchers.IO) {
            val command =
                buildString {
                    appendSpaced(properties.javaLocation)
                    appendSpaced("-jar")
                    appendSpaced(properties.jarLocation)
                    appendSpaced("--spring.config.location=${properties.configFileLocation}")
                    appendSpaced("--mode=${mode.name}")
                    args.forEach { appendSpaced("--${it.name}=${it.value}") }
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
                { logger.info { "Triggered submission task $mode. Job Id: ${it.id}. ${it.logsPath}. args: '${args.joinToString()}'" } },
                { throw it },
            )
        }

    private fun StringBuilder.appendSpaced(value: String) {
        append(value)
        append(" ")
    }
}

data class ExecutionArg(
    val name: String,
    val value: Any,
)
