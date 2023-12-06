package uk.ac.ebi.biostd.client.cluster.api

import arrow.core.Try
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.io.File

class LocalClusterClient : ClusterClient {
    override suspend fun triggerJob(jobSpec: JobSpec): Try<Job> {
        return withContext(Dispatchers.IO) {
            val logFile = File.createTempFile("prefix-", "-suffix");
            val jobId = executeProcess(listOf(jobSpec.command), logFile)
            Try.just(Job(jobId.toString(), LOCAL_QUEUE, logFile.absolutePath))
        }
    }

    override suspend fun awaitJob(jobSpec: JobSpec, checkJobInterval: Long, maxSecondsDuration: Long): Job {
        return triggerJob(jobSpec).fold({ throw it }, { it })
    }

    companion object {
        internal const val LOCAL_QUEUE = "local"
    }

    private fun executeProcess(params: List<String>, logFile: File): Long {
        val processBuilder = ProcessBuilder(params)
        processBuilder.redirectOutput(logFile)
        val process = processBuilder.start()
        process.waitFor()
        return process.pid()
    }
}
