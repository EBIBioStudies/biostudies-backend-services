package uk.ac.ebi.scheduler.releaser.api

import ac.uk.ebi.cluster.client.model.Job
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserService

@RestController
internal class SubmissionReleaserResource(private val submissionReleaserService: SubmissionReleaserService) {
    @PostMapping("/api/releaser")
    @ResponseBody
    fun releaseSubmissions(): Job = submissionReleaserService.triggerSubmissionReleaser()
}
