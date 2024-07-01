package ac.uk.ebi.biostd.cluster.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec
import uk.ac.ebi.biostd.client.cluster.model.QueueSpec

@RestController
@RequestMapping("/cluster")
class ClusterOperationsResource(
    private val clusterExecutor: ClusterExecutor,
) {
    @PostMapping("/{cluster}/health")
    suspend fun clusterHealthCheck(
        @PathVariable cluster: String,
    ): Job {
        val spec = JobSpec(command = "echo 'hello';")
        return clusterExecutor.triggerJobSync(Cluster.fromName(cluster), spec)
    }

    @PostMapping("/{cluster}/submit")
    suspend fun submitJob(
        @PathVariable cluster: String,
        @RequestBody job: JobSpecDto,
    ): Job {
        val response = clusterExecutor.triggerJobAsync(Cluster.fromName(cluster), job.asJobSpec())
        return response.fold(
            { it },
            { throw IllegalStateException(it) },
        )
    }

    @GetMapping("/{cluster}/jobs/{jobId}/status")
    suspend fun jobStatus(
        @PathVariable cluster: String,
        @PathVariable jobId: String,
    ): JobStatus {
        return JobStatus(clusterExecutor.jobStatus(Cluster.fromName(cluster), jobId))
    }

    @GetMapping("/{cluster}/jobs/{jobId}/logs")
    suspend fun jobLogs(
        @PathVariable cluster: String,
        @PathVariable jobId: String,
    ): String {
        return clusterExecutor.jobLogs(Cluster.fromName(cluster), jobId)
    }

    data class JobSpecDto(val command: String, val queue: String, val ramMegaBytes: Int) {
        fun asJobSpec(): JobSpec =
            JobSpec(
                command = command,
                queue = QueueSpec.fromName(queue),
                ram = MemorySpec.fromMegaBytes(ramMegaBytes),
            )
    }

    data class JobStatus(val status: String)
}
