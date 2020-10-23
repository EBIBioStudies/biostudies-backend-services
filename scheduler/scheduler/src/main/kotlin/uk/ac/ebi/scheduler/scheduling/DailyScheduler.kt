package uk.ac.ebi.scheduler.scheduling

import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService
import org.springframework.scheduling.annotation.Scheduled
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserService

internal class DailyScheduler(
    private val pmcLoaderService: PmcLoaderService,
    private val submissionReleaserService: SubmissionReleaserService
) {
    @Scheduled(cron = "0 0 6 * * *")
    fun dailyLoad() {
        pmcLoaderService.loadFile("/nfs/production3/ma/home/biostudy/EPMC-export/daily")
    }

    @Scheduled(cron = "0 0 7 * * *")
    fun dailyProcess() {
        pmcLoaderService.triggerProcessor()
    }

    @Scheduled(cron = "0 0 8 * * *")
    fun dailySubmission() {
        pmcLoaderService.triggerSubmitter()
    }

    @Scheduled(cron = "0 0 9 * * *")
    fun dailyRelease() {
        submissionReleaserService.triggerSubmissionReleaser()
    }
}
