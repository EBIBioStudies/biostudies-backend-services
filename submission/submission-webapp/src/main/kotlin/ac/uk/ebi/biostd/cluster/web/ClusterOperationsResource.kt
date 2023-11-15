package ac.uk.ebi.biostd.cluster.web

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
