package uk.ac.ebi.scheduler.scheduling

import org.springframework.scheduling.annotation.Scheduled
import uk.ac.ebi.scheduler.common.properties.DailyScheduling
import uk.ac.ebi.scheduler.pmc.exporter.domain.ExporterTrigger
import uk.ac.ebi.scheduler.pmc.importer.DEFAULT_FOLDER
import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserTrigger

internal class DailyScheduler(
    private val dailyScheduling: DailyScheduling,
    private val exporterTrigger: ExporterTrigger,
    private val pmcLoaderService: PmcLoaderService,
    private val submissionReleaserTrigger: SubmissionReleaserTrigger,
) {
    @Scheduled(cron = "0 0 1 * * *")
    fun releaseSubmissions() {
        if (dailyScheduling.releaser) submissionReleaserTrigger.triggerSubmissionReleaser()
    }

    @Scheduled(cron = "0 0 6 * * *")
    fun loadPmc() {
        if (dailyScheduling.pmc) pmcLoaderService.loadFile(DEFAULT_FOLDER)
    }

    @Scheduled(cron = "0 0 7 * * *")
    fun processPmc() {
        if (dailyScheduling.pmc) pmcLoaderService.triggerProcessor()
    }

    @Scheduled(cron = "0 0 8 * * *")
    fun submitPmc() {
        if (dailyScheduling.pmc) pmcLoaderService.triggerSubmitter()
    }

    @Scheduled(cron = "0 0 10 * * *")
    fun notifySubmissionRelease() {
        if (dailyScheduling.notifier) submissionReleaserTrigger.triggerSubmissionReleaseNotifier()
    }

    @Scheduled(cron = "0 0 20 * * *")
    fun exportPmcSubmissions() {
        if (dailyScheduling.pmc) exporterTrigger.triggerPmcExport()
    }

    @Scheduled(cron = "0 0 21 * * *")
    fun exportPublicSubmissions() {
        if (dailyScheduling.exporter) exporterTrigger.triggerPublicExport()
    }
}
