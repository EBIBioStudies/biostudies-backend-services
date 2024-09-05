package ac.uk.ebi.biostd.archive

import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ArchiveScheduler(
    private val persistenceService: SubmissionRequestPersistenceService,
) {
    /**
     * Archive request every day at 3 am.
     */
    @Scheduled(cron = "0 0 3 * * *")
    fun archiveRequests() =
        runBlocking {
            persistenceService
                .findAllProcessed()
                .onEach { (accNo, version) -> logger.info { "Archiving request $accNo, $version" } }
                .collect({ (accNo, version) ->
                    persistenceService.archiveRequest(accNo, version)
                    logger.info { "Archived request $accNo, $version" }
                })
        }
}
