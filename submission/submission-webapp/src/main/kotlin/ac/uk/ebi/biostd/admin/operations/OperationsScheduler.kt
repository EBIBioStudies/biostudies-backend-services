package ac.uk.ebi.biostd.admin.operations

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.migration.service.MigrationService
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService.CleanUpMode.NOTIFY
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled

class OperationsScheduler(
    private val properties: ApplicationProperties,
    private val operationsService: OperationsService,
    private val migrationService: MigrationService,
    private val statsReporterService: StatsReporterService,
    private val userCleanUpService: ExtUserSpaceCleanUpService,
) {
    /**
     * Delete request files every day at 1 am.
     */
    @Scheduled(cron = "0 0 1 * * *")
    fun deleteRequestFiles() {
        runBlocking { if (properties.enableTmpCleaning) operationsService.deleteRequestFiles() }
    }

    /**
     * Migrate released submissions from NFS to FIRE every day at 2 am.
     */
    @Scheduled(cron = "0 0 2 * * *")
    fun migrateSubmission() {
        runBlocking { if (properties.migration.enabled) migrationService.migrateSubmissions() }
    }

    /**
     * Archive request every day at 4 am.
     */
    @Scheduled(cron = "0 0 4 * * *")
    fun archiveRequests() {
        runBlocking { if (properties.enableTmpCleaning) operationsService.archiveRequests() }
    }

    /**
     * Send user space cleanup notifications every day at 6 am.
     */
    @Scheduled(cron = "0 0 6 * * *")
    fun sendUserSpaceCleanUpNotifications() {
        runBlocking { if (properties.cleanUp.enabled) userCleanUpService.cleanUp(NOTIFY, remote = true) }
    }

    /**
     * Clean files in temp folders every week, on Sundays, at 5 am
     */
    @Scheduled(cron = "0 0 5 * * 0")
    fun cleanTempFolders() {
        runBlocking { if (properties.enableTmpCleaning) operationsService.cleanTempFolders() }
    }

    /**
     * Generate the submission stats report on the 4th day of every month at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 4 * *")
    fun publishSubmissionStatsReport() {
        runBlocking { if (properties.enableStatsReport) statsReporterService.reportStats() }
    }
}
