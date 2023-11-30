package uk.ac.ebi.biostd.client.cluster.api

import arrow.core.Try
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.util.UUID

class LocalClusterClient : ClusterOperations {
    override suspend fun triggerJob(jobSpec: JobSpec): Try<Job> {
        return withContext(Dispatchers.IO) {
            Runtime.getRuntime().exec(jobSpec.command)
            val jobId = UUID.randomUUID().toString()
            Try.just(Job(jobId, LOCAL_QUEUE, ""))
        }
    }

    override suspend fun awaitJob(jobSpec: JobSpec, checkJobInterval: Long, maxSecondsDuration: Long): Job {
        return triggerJob(jobSpec).fold({ throw it }, { it })
    }

    companion object {
        internal const val LOCAL_QUEUE = "local"
    }
}
