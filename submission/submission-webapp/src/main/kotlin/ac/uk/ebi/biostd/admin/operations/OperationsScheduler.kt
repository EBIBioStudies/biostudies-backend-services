package ac.uk.ebi.biostd.admin.operations

import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled

class OperationsScheduler(
    private val operationsService: OperationsService,
) {
    /**
     * Delete request files every day at 1 am.
     */
    @Scheduled(cron = "0 0 1 * * *")
    fun deleteRequestFiles() = runBlocking { operationsService.deleteRequestFiles() }

    /**
     * Archive request every day at 4 am.
     */
    @Scheduled(cron = "0 0 4 * * *")
    fun archiveRequests() = runBlocking { operationsService.archiveRequests() }

    /**
     * Clean files in temp folders every week, on Sundays, at 5 am
     */
    @Scheduled(cron = "0 0 5 * * 0")
    fun cleanTempFolders() = runBlocking { operationsService.cleanTempFolders() }
}
