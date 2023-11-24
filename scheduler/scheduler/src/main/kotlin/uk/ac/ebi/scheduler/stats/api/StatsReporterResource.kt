package uk.ac.ebi.scheduler.stats.api

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.scheduler.stats.domain.StatsReporterTrigger

@RestController
internal class StatsReporterResource(
    private val statsReporterTrigger: StatsReporterTrigger,
) {
    @PostMapping("/api/stats/report")
    @ResponseBody
    suspend fun reportSubmissionStats(): Job = statsReporterTrigger.triggerStatsReporter()
}
