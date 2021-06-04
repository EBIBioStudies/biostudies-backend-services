package uk.ac.ebi.scheduler.scheduling

import ebi.ac.uk.base.ifTrue
import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService
import org.springframework.scheduling.annotation.Scheduled
import uk.ac.ebi.scheduler.common.properties.DailyScheduling
import uk.ac.ebi.scheduler.exporter.domain.ExporterTrigger
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserTrigger

internal class DailyScheduler(
    private val dailyScheduling: DailyScheduling,
    private val exporterTrigger: ExporterTrigger,
    private val pmcLoaderService: PmcLoaderService,
    private val submissionReleaserTrigger: SubmissionReleaserTrigger
) {
    @Scheduled(cron = "0 0 6 * * *")
    fun loadPmc() {
        dailyScheduling.pmc.ifTrue { pmcLoaderService.loadFile("/nfs/production3/ma/home/biostudy/EPMC-export/daily") }
    }

    @Scheduled(cron = "0 0 7 * * *")
    fun processPmc() {
        dailyScheduling.pmc.ifTrue { pmcLoaderService.triggerProcessor() }
    }

    @Scheduled(cron = "0 0 8 * * *")
    fun submitPmc() {
        dailyScheduling.pmc.ifTrue { pmcLoaderService.triggerSubmitter() }
    }

    @Scheduled(cron = "0 0 9 * * *")
    fun releaseSubmissions() {
        dailyScheduling.releaser.ifTrue { submissionReleaserTrigger.triggerSubmissionReleaser() }
    }

    @Scheduled(cron = "0 0 10 * * *")
    fun notifySubmissionRelease() {
        dailyScheduling.notifier.ifTrue { submissionReleaserTrigger.triggerSubmissionReleaseNotifier() }
    }

    @Scheduled(cron = "0 0 20 * * *")
    fun exportPublicSubmissions() {
        dailyScheduling.exporter.ifTrue { exporterTrigger.triggerPublicExport() }
    }
}
