package uk.ac.ebi.biostd.client.cluster.api

import arrow.core.Try
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec

interface ClusterOperations {
    suspend fun triggerJob(jobSpec: JobSpec): Try<Job>

    suspend fun awaitJob(
        jobSpec: JobSpec,
        checkJobInterval: Long = 30,
        maxSecondsDuration: Long = 60,
    ): Job
}
