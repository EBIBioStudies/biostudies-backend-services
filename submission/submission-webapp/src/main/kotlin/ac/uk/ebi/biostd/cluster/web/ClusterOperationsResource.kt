package ac.uk.ebi.biostd.cluster.web

import jakarta.validation.constraints.Pattern
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient

@RestController
@RequestMapping("/cluster")
@PreAuthorize("hasAuthority('ADMIN')")
@Validated
class ClusterOperationsResource(
    private val clusterClient: ClusterClient,
) {
    @GetMapping("/jobs/{jobId}/status")
    suspend fun jobStatus(
        @Pattern(regexp = JOB_ID_PATTERN)
        @PathVariable jobId: String,
    ): JobStatus = JobStatus(clusterClient.jobStatus(jobId))

    @GetMapping("/jobs/{jobId}/logs")
    suspend fun jobLogs(
        @Pattern(regexp = JOB_ID_PATTERN)
        @PathVariable jobId: String,
    ): String = clusterClient.jobLogs(jobId)

    data class JobStatus(
        val status: String,
    )

    private companion object {
        const val JOB_ID_PATTERN = "[A-Za-z0-9._-]{1,64}"
    }
}
