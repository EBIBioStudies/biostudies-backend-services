package ac.uk.ebi.biostd.cluster.web

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.api.ClusterOperations
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec

@RestController
@RequestMapping("/cluster")
class ClusterOperationsResource(
    private val clusterOperations: ClusterOperations,
) {
    @PostMapping("/health")
    suspend fun clusterHealthCheck(): Job {
        val spec = JobSpec(command = "echo 'hello world'")
        return clusterOperations.awaitJob(spec)
    }
}
