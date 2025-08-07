package uk.ac.ebi.scheduler.scheduling

import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import uk.ac.ebi.scheduler.common.properties.DailyScheduling
import uk.ac.ebi.scheduler.pmc.exporter.domain.ExporterTrigger
import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserTrigger
import uk.ac.ebi.scheduler.stats.domain.StatsReporterTrigger

const val DAILY_PMC_SUB_LIMIT = 5_000

internal class DailyScheduler(
    private val dailyScheduling: DailyScheduling,
    private val exporterTrigger: ExporterTrigger,
    private val pmcLoaderService: PmcLoaderService,
    private val statsReporterTrigger: StatsReporterTrigger,
    private val submissionReleaserTrigger: SubmissionReleaserTrigger,
) {
    // Execute at 02:00 am
    @Scheduled(cron = "0 0 2 * * *")
    fun releaseSubmissions() =
        runBlocking {
            if (dailyScheduling.releaser) submissionReleaserTrigger.triggerSubmissionReleaser()
        }

    // Execute at 06:00 am
    @Scheduled(cron = "0 0 6 * * *")
    fun loadPmc() =
        runBlocking {
            if (dailyScheduling.pmcImport) pmcLoaderService.loadFile()
        }

    // Execute at 07:00 am
    @Scheduled(cron = "0 0 7 * * *")
    fun processPmc() =
        runBlocking {
            if (dailyScheduling.pmcImport) pmcLoaderService.triggerProcessor()
        }

    // Execute at 04:00 am
    @Scheduled(cron = "0 0 4 * * *")
    fun submitPmc() =
        runBlocking {
            if (dailyScheduling.pmcImport) pmcLoaderService.triggerSubmitter(limit = DAILY_PMC_SUB_LIMIT)
        }

    // Execute at 10:00 am
    @Scheduled(cron = "0 0 10 * * *")
    fun notifySubmissionRelease() =
        runBlocking {
            if (dailyScheduling.notifier) submissionReleaserTrigger.triggerSubmissionReleaseNotifier()
        }

    // Execute at 4:00 am
    @Scheduled(cron = "0 0 4 * * *")
    fun pmcViewUpdate() =
        runBlocking {
            if (dailyScheduling.pmcExport) exporterTrigger.triggerPmcViewUpdate()
        }

    // Execute at 8:00 pm / 20:00
    @Scheduled(cron = "0 0 20 * * *")
    fun exportPmcSubmissions() =
        runBlocking {
            if (dailyScheduling.pmcExport) exporterTrigger.triggerPmcExport()
        }

    // Execute at 9:00 pm / 21:00
    @Scheduled(cron = "0 0 21 * * *")
    fun exportPublicSubmissions() =
        runBlocking {
            if (dailyScheduling.exporter) exporterTrigger.triggerPublicExport()
        }

    // 3:00 AM on the 4th day of every month
    @Scheduled(cron = "0 0 3 4 * *")
    fun publishSubmissionStatsReport() =
        runBlocking {
            if (dailyScheduling.statsReporter) statsReporterTrigger.triggerStatsReporter()
        }
}
