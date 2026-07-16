package uk.ac.ebi.biostd.client.cluster.api

import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ClusterClient {
    suspend fun triggerJobAsync(jobSpec: JobSpec): Result<Job>

    suspend fun triggerJobSync(
        jobSpec: JobSpec,
        checkJobInterval: Duration = 30.seconds,
        maxDuration: Duration = 60.seconds,
    ): Job

    suspend fun jobStatus(jobId: String): String

    suspend fun jobLogs(jobId: String): String
}
