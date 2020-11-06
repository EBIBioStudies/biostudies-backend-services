package uk.ac.ebi.scheduler.releaser.api

import ac.uk.ebi.cluster.client.model.Job
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserTrigger

@RestController
internal class SubmissionReleaserResource(private val submissionReleaserTrigger: SubmissionReleaserTrigger) {
    @PostMapping("/api/releaser/release")
    @ResponseBody
    fun releaseSubmissions(): Job = submissionReleaserTrigger.triggerSubmissionReleaser()

    @PostMapping("/api/releaser/notify")
    @ResponseBody
    fun notifySubmissionsRelease(): Job = submissionReleaserTrigger.triggerSubmissionReleaseNotifier()
}
