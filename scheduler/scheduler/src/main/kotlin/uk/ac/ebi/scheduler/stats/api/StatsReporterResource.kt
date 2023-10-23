package uk.ac.ebi.scheduler.stats.api

import ac.uk.ebi.cluster.client.model.Job
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.scheduler.stats.domain.StatsReporterTrigger

@RestController
internal class StatsReporterResource(
    private val statsReporterTrigger: StatsReporterTrigger,
) {
    @PostMapping("/api/stats/report")
    @ResponseBody
    suspend fun reportSubmissionStats(): Job = statsReporterTrigger.triggerStatsReporter()
}
