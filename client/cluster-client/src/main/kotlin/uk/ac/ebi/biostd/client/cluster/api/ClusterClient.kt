package uk.ac.ebi.biostd.client.cluster.api

import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec

interface ClusterClient {
    suspend fun triggerJobAsync(jobSpec: JobSpec): Result<Job>

    suspend fun triggerJobSync(
        jobSpec: JobSpec,
        checkJobInterval: Long = 30,
        maxSecondsDuration: Long = 60,
    ): Job

    suspend fun jobStatus(jobId: String): String

    suspend fun jobLogs(jobId: String): String
}
