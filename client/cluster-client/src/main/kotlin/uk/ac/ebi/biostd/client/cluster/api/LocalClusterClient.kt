package uk.ac.ebi.biostd.client.cluster.api

import arrow.core.Try
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createTempFile

class LocalClusterClient : ClusterClient {
    private val activeProcess = ConcurrentHashMap<Long, Process>()

    override suspend fun triggerJobAsync(jobSpec: JobSpec): Try<Job> {
        return withContext(Dispatchers.IO) {
            val logFile = createTempFile().toFile()
            val processId = executeProcess(jobSpec.command, logFile)
            Try.just(Job(processId.toString(), LOCAL_QUEUE, logFile.absolutePath))
        }
    }

    override suspend fun jobStatus(jobId: String): String {
        val process = activeProcess.getValue(jobId.toLong())
        return if (process.isAlive) "RUNNING" else "DONE"
    }

    override suspend fun triggerJobSync(jobSpec: JobSpec, checkJobInterval: Long, maxSecondsDuration: Long): Job {
        return withContext(Dispatchers.IO) {
            val logFile = createTempFile().toFile()
            val processId = executeProcess(jobSpec.command, logFile)
            waitProcess(processId)
            return@withContext Job(processId.toString(), LOCAL_QUEUE, logFile.absolutePath)
        }
    }

    companion object {
        internal const val LOCAL_QUEUE = "local"
    }

    private fun executeProcess(command: String, logFile: File): Long {
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
//mkdir -m 710 -p /var/folders/ss/nj08mk515j9g013jnsv6dmc00000gp/T/ftpUser-ftp3747409275178958406/TEST/c1
///var/folders/ss/nj08mk515j9g013jnsv6dmc00000gp/T/ftpUser-ftp13632402090457427078/TEST
