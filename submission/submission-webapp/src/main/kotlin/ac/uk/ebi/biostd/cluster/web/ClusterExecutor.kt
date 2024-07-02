package ac.uk.ebi.biostd.cluster.web

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.common.properties.Cluster
import ac.uk.ebi.biostd.submission.config.GeneralConfig
import uk.ac.ebi.biostd.client.cluster.api.LsfClusterClient
import uk.ac.ebi.biostd.client.cluster.api.SlurmClusterClient
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec

class ClusterExecutor private constructor(
    val lsfClusterClient: LsfClusterClient,
    val slurmClusterClient: SlurmClusterClient,
) {
    constructor(properties: ApplicationProperties) : this(
        lsfClusterClient = GeneralConfig.lsfCluster(properties),
        slurmClusterClient = GeneralConfig.slurmCluster(properties),
    )

    suspend fun triggerJobAsync(
        cluster: Cluster,
        jobSpec: JobSpec,
    ): Result<Job> {
        return when (cluster) {
            Cluster.LSF -> lsfClusterClient.triggerJobAsync(jobSpec)
            Cluster.SLURM -> slurmClusterClient.triggerJobAsync(jobSpec)
        }
    }

    suspend fun triggerJobSync(
        cluster: Cluster,
        jobSpec: JobSpec,
    ): Job {
        return when (cluster) {
            Cluster.LSF -> lsfClusterClient.triggerJobSync(jobSpec)
            Cluster.SLURM -> slurmClusterClient.triggerJobSync(jobSpec)
        }
    }

    suspend fun jobStatus(
        cluster: Cluster,
        jobId: String,
    ): String {
        return when (cluster) {
            Cluster.LSF -> lsfClusterClient.jobStatus(jobId)
            Cluster.SLURM -> slurmClusterClient.jobStatus(jobId)
        }
    }

    suspend fun jobLogs(
        cluster: Cluster,
        jobId: String,
    ): String {
        return when (cluster) {
            Cluster.LSF -> lsfClusterClient.jobLogs(jobId)
            Cluster.SLURM -> slurmClusterClient.jobLogs(jobId)
        }
    }
}
