package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.properties.TaskHostProperties
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.domain.extended.LocalExtSubmissionSubmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File
import java.util.UUID

private val logger = KotlinLogging.logger {}

class DynamicExtSubmissionSubmitter(
    private val extSubmissionSubmitter: LocalExtSubmissionSubmitter,
    private val hostProperties: TaskHostProperties,
) : ExtSubmissionSubmitter by extSubmissionSubmitter {

    override suspend fun processRequest(accNo: String, version: Int) {
        when (hostProperties.enableTaskMode) {
            true -> copyRequestFilesRemotly(accNo, version)
            false -> extSubmissionSubmitter.processRequest(accNo, version)
        }
    }

    private suspend fun copyRequestFilesRemotly(accNo: String, version: Int) = withContext(Dispatchers.IO) {
        val pId = UUID.randomUUID()
        val logs = File(hostProperties.logsLocation, "application-$pId.log")
        val params = buildList<String> {
            add("java")
            add("-jar")
            add(hostProperties.jarLocation)
            add("--spring.config.location=${hostProperties.configFilePath}")
            add("--accNo=$accNo")
            add("--version=$version")
            add("--mode=COPY")
        }

        logger.info { "$accNo $version process $pId, task ='${params.joinToString(" ")}', logs='${logs.absolutePath}'" }
        val processBuilder = ProcessBuilder(params)
        processBuilder.redirectOutput(logs)
        val process = processBuilder.start()
        val exitCode = process.waitFor()
        if (exitCode != 0) throw IllegalStateException("Failed to process subsmision '$accNo' in process  $pId")
    }
}
