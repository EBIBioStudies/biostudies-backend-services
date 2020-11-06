package uk.ac.ebi.scheduler.scheduling

import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService
import org.springframework.scheduling.annotation.Scheduled
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserTrigger

internal class DailyScheduler(
    private val pmcLoaderService: PmcLoaderService,
    private val submissionReleaserTrigger: SubmissionReleaserTrigger
) {
    @Scheduled(cron = "0 0 6 * * *")
    fun loadPmc() {
        pmcLoaderService.loadFile("/nfs/production3/ma/home/biostudy/EPMC-export/daily")
    }

    @Scheduled(cron = "0 0 7 * * *")
    fun processPmc() {
        pmcLoaderService.triggerProcessor()
    }

    @Scheduled(cron = "0 0 8 * * *")
    fun submitPmc() {
        pmcLoaderService.triggerSubmitter()
    }

    @Scheduled(cron = "0 0 9 * * *")
    fun releaseSubmissions() {
        submissionReleaserTrigger.triggerSubmissionReleaser()
    }

    @Scheduled(cron = "0 0 10 * * *")
    fun notifySubmissionRelease() {
        submissionReleaserTrigger.triggerSubmissionReleaseNotifier()
    }
}
