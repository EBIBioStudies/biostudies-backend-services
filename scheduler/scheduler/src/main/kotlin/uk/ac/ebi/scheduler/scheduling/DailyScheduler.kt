package uk.ac.ebi.scheduler.scheduling

import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import uk.ac.ebi.scheduler.common.properties.DailyScheduling
import uk.ac.ebi.scheduler.pmc.exporter.domain.ExporterTrigger
import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserTrigger

const val DAILY_PMC_SUB_LIMIT = 5_000

internal class DailyScheduler(
    private val dailyScheduling: DailyScheduling,
    private val exporterTrigger: ExporterTrigger,
    private val pmcLoaderService: PmcLoaderService,
    private val submissionReleaserTrigger: SubmissionReleaserTrigger,
) {
    /**
     * Release submissions every day at 02:00
     */
    @Scheduled(cron = "0 0 2 * * *")
    fun releaseSubmissions() =
        runBlocking {
            if (dailyScheduling.releaser) submissionReleaserTrigger.triggerSubmissionReleaser()
        }

    /**
     * Load PMC submissions every day at 03:00
     */
    @Scheduled(cron = "0 0 3 * * *")
    fun loadPmc() =
        runBlocking {
            if (dailyScheduling.pmcImport) pmcLoaderService.loadFile()
        }

    /**
     * Process PMC submissions every day at 04:00
     */
    @Scheduled(cron = "0 0 4 * * *")
    fun processPmc() =
        runBlocking {
            if (dailyScheduling.pmcImport) pmcLoaderService.triggerProcessor()
        }

    /**
     * Submit PMC submissions every day at 05:00
     */
    @Scheduled(cron = "0 0 5 * * *")
    fun submitPmc() =
        runBlocking {
            if (dailyScheduling.pmcImport) pmcLoaderService.triggerSubmitter(limit = DAILY_PMC_SUB_LIMIT)
        }

    /**
     * Send submission release notifications every day at 10:00
     */
    @Scheduled(cron = "0 0 10 * * *")
    fun notifySubmissionRelease() =
        runBlocking {
            if (dailyScheduling.notifier) submissionReleaserTrigger.triggerSubmissionReleaseNotifier()
        }

    /**
     * Update PMC view every day at 06:00
     */
    @Scheduled(cron = "0 0 6 * * *")
    fun pmcViewUpdate() =
        runBlocking {
            if (dailyScheduling.pmcExport) exporterTrigger.triggerPmcViewUpdate()
        }

    /**
     * Export PMC submissions every day at 20:00
     */
    @Scheduled(cron = "0 0 20 * * *")
    fun exportPmcSubmissions() =
        runBlocking {
            if (dailyScheduling.pmcExport) exporterTrigger.triggerPmcExport()
        }

    /**
     * Export public submissions every day at 21:00
     */
    @Scheduled(cron = "0 0 21 * * *")
    fun exportPublicSubmissions() =
        runBlocking {
            if (dailyScheduling.exporter) exporterTrigger.triggerPublicExport()
        }
}
