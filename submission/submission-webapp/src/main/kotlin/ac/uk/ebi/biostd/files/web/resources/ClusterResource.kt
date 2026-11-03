package ac.uk.ebi.biostd.files.web.resources

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient

@RestController
@PreAuthorize("isAuthenticated()")
class ClusterResource(
    private val clusterClient: ClusterClient,
) {
    @GetMapping("/cluster/job/{jobId}/status")
    @ResponseBody
    suspend fun jobStatus(
        @PathVariable jobId: String,
    ): String {
        require(jobId.isNotEmpty() && jobId.all(Char::isDigit)) { "Invalid cluster job id: $jobId" }
        return clusterClient.jobStatus(jobId).trim()
    }
}
