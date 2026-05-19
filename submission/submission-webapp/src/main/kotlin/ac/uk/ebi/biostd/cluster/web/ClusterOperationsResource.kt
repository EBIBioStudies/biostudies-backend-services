package ac.uk.ebi.biostd.cluster.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec
import uk.ac.ebi.biostd.client.cluster.model.QueueSpec

@RestController
@RequestMapping("/cluster")
@Tag(name = "Cluster Operations", description = "Internal cluster job submission, status and log operations.")
class ClusterOperationsResource(
    private val clusterClient: ClusterClient,
) {
    @Operation(summary = "Submit a cluster health-check job")
    @PostMapping("/health")
    suspend fun clusterHealthCheck(): Job {
        val spec = JobSpec(command = "echo 'hello';")
        return clusterClient.triggerJobSync(spec)
    }

    @Operation(summary = "Submit a cluster job")
    @PostMapping("/submit")
    suspend fun submitJob(
        @RequestBody job: JobSpecDto,
    ): Job {
        val response = clusterClient.triggerJobAsync(job.asJobSpec())
        return response.fold(
            { it },
            { throw IllegalStateException(it) },
        )
    }

    @Operation(summary = "Get cluster job status")
    @GetMapping("/jobs/{jobId}/status")
    suspend fun jobStatus(
        @PathVariable jobId: String,
    ): JobStatus {
        return JobStatus(clusterClient.jobStatus(jobId))
    }

    @Operation(summary = "Get cluster job logs")
    @GetMapping("/jobs/{jobId}/logs")
    suspend fun jobLogs(
        @PathVariable jobId: String,
    ): String {
        return clusterClient.jobLogs(jobId)
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
