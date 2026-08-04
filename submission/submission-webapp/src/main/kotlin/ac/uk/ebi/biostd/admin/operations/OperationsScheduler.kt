package ac.uk.ebi.biostd.admin.operations

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.migration.service.MigrationService
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService.CleanUpMode.CLEAN_UP
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService.CleanUpMode.NOTIFY
import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor
import ac.uk.ebi.biostd.submission.pmc.ProcessConfig
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled

class OperationsScheduler(
    private val properties: ApplicationProperties,
    private val operationsService: OperationsService,
    private val migrationService: MigrationService,
    private val pmcLinksProcessor: PmcLinksProcessor,
    private val statsReporterService: StatsReporterService,
    private val userCleanUpService: ExtUserSpaceCleanUpService,
) {
    /**
     * Delete request files every day at 01:00
     */
    @Scheduled(cron = "0 0 1 * * *")
    fun deleteRequestFiles() {
        runBlocking { if (properties.enableTmpCleaning) operationsService.deleteRequestFiles() }
    }

    /**
     * Migrate released submissions from NFS to FIRE every day at 02:00
     */
    @Scheduled(cron = "0 0 2 * * *")
    fun migrateSubmission() {
        runBlocking { if (properties.migration.enabled) migrationService.migrateSubmissions() }
    }

    /**
     * Archive request every day at 04:00
     */
    @Scheduled(cron = "0 0 4 * * *")
    fun archiveRequests() {
        runBlocking { if (properties.enableTmpCleaning) operationsService.archiveRequests() }
    }

    /**
     * Send user space cleanup notifications every day at 06:00
     */
    @Scheduled(cron = "0 0 6 * * *")
    fun sendUserSpaceCleanUpNotifications() {
        runBlocking {
            if (properties.cleanUp.userSpaceCleanUpEnabled) userCleanUpService.cleanUp(NOTIFY, remote = true)
        }
    }

    /**
     * Clean inactive user spaces every day at 23:00
     */
    @Scheduled(cron = "0 0 23 * * *")
    fun cleanUserSpaces() {
        runBlocking {
            if (properties.cleanUp.userSpaceCleanUpEnabled) userCleanUpService.cleanUp(CLEAN_UP, remote = true)
        }
    }

    /**
     * Clean files in temp folders every week, on Sundays, at 05:00
     */
    @Scheduled(cron = "0 0 5 * * 0")
    fun cleanTempFolders() {
        runBlocking { if (properties.enableTmpCleaning) operationsService.cleanTempFolders() }
    }

    /**
     * Generate the submission stats report on the 4th day of every month at 03:00
     */
    @Scheduled(cron = "0 0 3 4 * *")
    fun publishSubmissionStatsReport() {
        runBlocking { if (properties.enableStatsReport) statsReporterService.reportStats() }
    }

    /**
     * Extract PMC links every at the configured rate in milliseconds with a delay of 5 seconds between executions
     */
    @Scheduled(fixedRateString = "\${app.pmc.rateMiliseconds}", initialDelay = INITIAL_DELAY_MS)
    fun extractPmcLinks() {
        runBlocking {
            if (properties.pmc.enableLinksExtraction) {
                pmcLinksProcessor.loadFromDb(ProcessConfig(limit = properties.pmc.loadLimit))
            }
        }
    }

    companion object {
        private const val INITIAL_DELAY_MS = 5000L
    }
}
