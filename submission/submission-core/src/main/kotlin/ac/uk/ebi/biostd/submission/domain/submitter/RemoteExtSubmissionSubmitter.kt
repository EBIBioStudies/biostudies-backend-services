package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class RemoteExtSubmissionSubmitter(
    private val submissionTaskProperties: SubmissionTaskProperties,
) : ExtSubmissionSubmitter {
    override suspend fun createRequest(rqt: ExtSubmitRequest): Pair<String, Int> {
        TODO("Not yet implemented")
    }

    override suspend fun indexRequest(accNo: String, version: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun loadRequest(accNo: String, version: Int) {
        executeRemotly(accNo, version, Mode.LOAD)
    }

    override suspend fun cleanRequest(accNo: String, version: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun processRequest(accNo: String, version: Int) {
        executeRemotly(accNo, version, Mode.COPY)
    }

    override suspend fun checkReleased(accNo: String, version: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun saveRequest(accNo: String, version: Int): ExtSubmission {
        TODO("Not yet implemented")
    }

    override suspend fun finalizeRequest(accNo: String, version: Int): ExtSubmission {
        TODO("Not yet implemented")
    }

    override suspend fun release(accNo: String) {
        TODO("Not yet implemented")
    }

    override suspend fun handleRequest(accNo: String, version: Int): ExtSubmission {
        TODO("Not yet implemented")
    }

    private suspend fun executeRemotly(accNo: String, version: Int, mode: Mode) = withContext(Dispatchers.IO) {
        val pId = UUID.randomUUID()
        val logs = File(submissionTaskProperties.logsLocation, "application-$pId.log")
        val params = buildList<String> {
            add("java")
            add("-jar")
            add(submissionTaskProperties.jarLocation)
            add("--spring.config.location=${submissionTaskProperties.configFilePath}")
            add("--accNo=$accNo")
            add("--version=$version")
            add("--mode=${mode.name}")
        }
        logger.info { "$accNo $version process $pId, task ='${params.joinToString(" ")}', logs='${logs.absolutePath}'" }
        val exitCode = executeRemotly(logs, params)
        if (exitCode != 0) throw IllegalStateException("Failed to process subsmision '$accNo' in process  $pId")
    }

    private fun executeRemotly(logs: File, params: List<String>): Int {
        val processBuilder = ProcessBuilder(params)
        processBuilder.redirectOutput(logs)
        val process = processBuilder.start()
        return process.waitFor()
    }
}
