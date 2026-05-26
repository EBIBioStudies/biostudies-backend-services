package ac.uk.ebi.biostd.admin.operations

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.migration.service.MigrationService
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled

class OperationsScheduler(
    private val applicationProperties: ApplicationProperties,
    private val operationsService: OperationsService,
    private val migrationService: MigrationService,
    private val statsReporterService: StatsReporterService,
) {
    /**
     * Delete request files every day at 1 am.
     */
    @Scheduled(cron = "0 0 1 * * *")
    fun deleteRequestFiles() {
        runBlocking { if (applicationProperties.enableTmpCleaning) operationsService.deleteRequestFiles() }
    }

    /**
     * Archive request every day at 4 am.
     */
    @Scheduled(cron = "0 0 4 * * *")
    fun archiveRequests() {
        runBlocking { if (applicationProperties.enableTmpCleaning) operationsService.archiveRequests() }
    }

    /**
     * Clean files in temp folders every week, on Sundays, at 5 am
     */
    @Scheduled(cron = "0 0 5 * * 0")
    fun cleanTempFolders() {
        runBlocking { if (applicationProperties.enableTmpCleaning) operationsService.cleanTempFolders() }
    }

    /**
     * Generate the submission stats report on the 4th day of every month at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 4 * *")
    fun publishSubmissionStatsReport() {
        runBlocking { if (applicationProperties.enableStatsReport) statsReporterService.reportStats() }
    }

    @Scheduled(cron = "0 0 * * * *")
    fun migrateSubmission() {
        runBlocking {
            if (applicationProperties.migration.enabled) {
                migrationService.migrateSubmissions()
            }
        }
    }
}
