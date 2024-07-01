package uk.ac.ebi.biostd.client.cluster.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.Result.Companion.success
import kotlin.io.path.createTempFile

class LocalClusterClient : ClusterClient {
    private val activeProcess = ConcurrentHashMap<Long, Process>()
    private val jobLogs = ConcurrentHashMap<Long, File>()

    override suspend fun triggerJobAsync(jobSpec: JobSpec): Result<Job> {
        return withContext(Dispatchers.IO) {
            val logFile = createTempFile().toFile()
            val processId = executeProcess(jobSpec.command, logFile)

            jobLogs[processId] = logFile
            success(Job(processId.toString(), LOCAL_QUEUE))
        }
    }

    override suspend fun triggerJobSync(
        jobSpec: JobSpec,
        checkJobInterval: Long,
        maxSecondsDuration: Long,
    ): Job {
        return withContext(Dispatchers.IO) {
            val logFile = createTempFile().toFile()
            val processId = executeProcess(jobSpec.command, logFile)
            waitProcess(processId)
            return@withContext Job(processId.toString(), LOCAL_QUEUE)
        }
    }

    override suspend fun jobStatus(jobId: String): String {
        val process = activeProcess.getValue(jobId.toLong())
        return if (process.isAlive) "RUNNING" else "DONE"
    }

    override suspend fun jobLogs(jobId: String): String {
        return jobLogs.getValue(jobId.toLong()).readText()
    }

    companion object {
        internal const val LOCAL_QUEUE = "local"
    }

    private fun executeProcess(
        command: String,
        logFile: File,
    ): Long {
        val processBuilder = ProcessBuilder(command.split(" "))
        processBuilder.redirectOutput(logFile)
        val process = processBuilder.start()
        val processId = process.pid()
        activeProcess[processId] = process
        return processId
    }

    private fun waitProcess(processId: Long) {
        val process = activeProcess.getValue(processId)
        process.waitFor()
    }
}
