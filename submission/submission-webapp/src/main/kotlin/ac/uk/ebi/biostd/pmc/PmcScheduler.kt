package ac.uk.ebi.biostd.pmc

import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor
import ac.uk.ebi.biostd.submission.pmc.ProcessConfig
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled

private val logger = KotlinLogging.logger {}

class PmcScheduler(
    private val pmcLinksProcessor: PmcLinksProcessor,
) {
    // Execute every 4 hours
    @Scheduled(cron = "0 0 */4 * * *")
    fun onSchedule() =
        runBlocking {
            logger.info { "Runninng scheduled PMC links loading limit = $LOAD_LIMIT, user = $USER_EMAIL" }
            pmcLinksProcessor.loadFromDb(ProcessConfig(limit = LOAD_LIMIT))
        }

    companion object {
        private const val LOAD_LIMIT = 5000
        private const val USER_EMAIL = "biostudies-dev@ebi.ac.uk"
    }
}
