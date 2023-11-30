package ac.uk.ebi.biostd.cluster.web

import arrow.core.Try
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.api.ClusterOperations
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec
import uk.ac.ebi.biostd.client.cluster.model.QueueSpec

@RestController
@RequestMapping("/cluster")
class ClusterOperationsResource(
    private val clusterOperations: ClusterOperations,
) {
    @PostMapping("/health")
    suspend fun clusterHealthCheck(): Job {
        val spec = JobSpec(command = "echo 'hello world'")
        return clusterOperations.triggerJobSync(spec)
    }

    @PostMapping("/submit")
    suspend fun submitJob(@RequestBody job: JobSpecDto): Job {
        return when (val response = clusterOperations.triggerJobAsync(job.asJobSpec())) {
            is Try.Failure -> throw IllegalStateException(response.exception)
            is Try.Success -> response.value
        }
    }

    @GetMapping("/jobs/{jobId}/status")
    suspend fun jobStatus(@PathVariable jobId: String): String {
        return clusterOperations.jobStatus(jobId)
    }

    data class JobSpecDto(val command: String, val queue: String, val ramMegaBytes: Int) {
        fun asJobSpec(): JobSpec = JobSpec(
            command = command, queue = QueueSpec.fromName(queue), ram = MemorySpec.fromMegaBytes(ramMegaBytes)
        )
    }
}
