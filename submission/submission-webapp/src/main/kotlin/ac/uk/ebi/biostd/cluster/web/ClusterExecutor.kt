package ac.uk.ebi.biostd.cluster.web

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.config.GeneralConfig
import uk.ac.ebi.biostd.client.cluster.api.LsfClusterClient
import uk.ac.ebi.biostd.client.cluster.api.SlurmClusterClient
import uk.ac.ebi.biostd.client.cluster.model.Cluster
import uk.ac.ebi.biostd.client.cluster.model.Cluster.LSF
import uk.ac.ebi.biostd.client.cluster.model.Cluster.SLURM
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec

/**
 * Temporally class added with the purpose of testing both cluster type access. Expected to be deleted when LSF service
 * is no longer operational.
 */
class ClusterExecutor private constructor(
    private val lsfClusterClient: LsfClusterClient,
    private val slurmClusterClient: SlurmClusterClient,
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
            LSF -> lsfClusterClient.triggerJobAsync(jobSpec)
            SLURM -> slurmClusterClient.triggerJobAsync(jobSpec)
        }
    }

    suspend fun triggerJobSync(
        cluster: Cluster,
        jobSpec: JobSpec,
    ): Job {
        return when (cluster) {
            LSF -> lsfClusterClient.triggerJobSync(jobSpec)
            SLURM -> slurmClusterClient.triggerJobSync(jobSpec)
        }
    }

    suspend fun jobStatus(
        cluster: Cluster,
        jobId: String,
    ): String {
        return when (cluster) {
            LSF -> lsfClusterClient.jobStatus(jobId)
            SLURM -> slurmClusterClient.jobStatus(jobId)
        }
    }

    suspend fun jobLogs(
        cluster: Cluster,
        jobId: String,
    ): String {
        return when (cluster) {
            LSF -> lsfClusterClient.jobLogs(jobId)
            SLURM -> slurmClusterClient.jobLogs(jobId)
        }
    }
}
