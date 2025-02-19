package uk.ac.ebi.scheduler.scheduling

import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import uk.ac.ebi.scheduler.common.properties.DailyScheduling
import uk.ac.ebi.scheduler.pmc.exporter.domain.ExporterTrigger
import uk.ac.ebi.scheduler.pmc.importer.DEFAULT_FOLDER
import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserTrigger
import uk.ac.ebi.scheduler.stats.domain.StatsReporterTrigger

internal class DailyScheduler(
    private val dailyScheduling: DailyScheduling,
    private val exporterTrigger: ExporterTrigger,
    private val pmcLoaderService: PmcLoaderService,
    private val statsReporterTrigger: StatsReporterTrigger,
    private val submissionReleaserTrigger: SubmissionReleaserTrigger,
) {
    @Scheduled(cron = "0 0 2 * * *")
    fun releaseSubmissions() =
        runBlocking {
            if (dailyScheduling.releaser) submissionReleaserTrigger.triggerSubmissionReleaser()
        }

    @Scheduled(cron = "0 0 6 * * *")
    fun loadPmc() =
        runBlocking {
            if (dailyScheduling.pmcImport) pmcLoaderService.loadFile(DEFAULT_FOLDER, file = null)
        }

    @Scheduled(cron = "0 0 7 * * *")
    fun processPmc() =
        runBlocking {
            if (dailyScheduling.pmcImport) pmcLoaderService.triggerProcessor()
        }

    @Scheduled(cron = "0 0 8 * * *")
    fun submitPmc() =
        runBlocking {
            if (dailyScheduling.pmcImport) pmcLoaderService.triggerSubmitter()
        }

    @Scheduled(cron = "0 0 10 * * *")
    fun notifySubmissionRelease() =
        runBlocking {
            if (dailyScheduling.notifier) submissionReleaserTrigger.triggerSubmissionReleaseNotifier()
        }

    @Scheduled(cron = "0 0 20 * * *")
    fun exportPmcSubmissions() =
        runBlocking {
            if (dailyScheduling.pmcExport) exporterTrigger.triggerPmcExport()
        }

    @Scheduled(cron = "0 0 21 * * *")
    fun exportPublicSubmissions() =
        runBlocking {
            if (dailyScheduling.exporter) exporterTrigger.triggerPublicExport()
        }

    @Scheduled(cron = "0 0 3 4 * *")
    fun publishSubmissionStatsReport() =
        runBlocking {
            if (dailyScheduling.statsReporter) statsReporterTrigger.triggerStatsReporter()
        }
}
