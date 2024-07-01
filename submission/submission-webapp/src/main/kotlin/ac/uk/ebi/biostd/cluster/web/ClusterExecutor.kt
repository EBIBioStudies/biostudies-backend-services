package ac.uk.ebi.biostd.cluster.web

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import uk.ac.ebi.biostd.client.cluster.api.LsfClusterClient
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec

class ClusterExecutor private constructor(val lsfClusterClient: LsfClusterClient) {
    constructor(properties: ApplicationProperties) : this(
        LsfClusterClient.create(
            properties.cluster.key,
            properties.cluster.server,
            properties.cluster.logsPath,
        ),
    )

    suspend fun triggerJobAsync(
        cluster: Cluster,
        jobSpec: JobSpec,
    ): Result<Job> {
        return when (cluster) {
            Cluster.LSF -> lsfClusterClient.triggerJobAsync(jobSpec)
        }
    }

    suspend fun triggerJobSync(
        cluster: Cluster,
        jobSpec: JobSpec,
    ): Job {
        return when (cluster) {
            Cluster.LSF -> lsfClusterClient.triggerJobSync(jobSpec)
        }
    }

    suspend fun jobStatus(
        cluster: Cluster,
        jobId: String,
    ): String {
        return when (cluster) {
            Cluster.LSF -> lsfClusterClient.jobStatus(jobId)
        }
    }

    suspend fun jobLogs(
        cluster: Cluster,
        jobId: String,
    ): String {
        return when (cluster) {
            Cluster.LSF -> lsfClusterClient.jobLogs(jobId)
        }
    }
}

enum class Cluster {
    LSF,
    ;

    companion object {
        fun fromName(name: String): Cluster {
            return when (name.uppercase()) {
                "LSF" -> LSF
                else -> throw IllegalArgumentException("$name is not a valid cluster name")
            }
        }
    }
}
