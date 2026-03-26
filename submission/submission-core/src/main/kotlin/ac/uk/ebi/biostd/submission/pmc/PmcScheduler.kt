package ac.uk.ebi.biostd.submission.pmc

import ac.uk.ebi.biostd.common.properties.PmcProperties
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled

private val logger = KotlinLogging.logger {}

class PmcScheduler(
    private val pmcLinksProcessor: PmcLinksProcessor,
    private val pmcProperties: PmcProperties,
) {
    // Execute every 4 hours
    @Scheduled(cron = "0 0 */4 * * *")
    fun onSchedule() {
        if (!pmcProperties.enableLinksExtraction) {
            runBlocking {
                logger.info { "Runninng scheduled PMC links loading limit = $LOAD_LIMIT, user = $USER_EMAIL" }
                pmcLinksProcessor.loadFromDb(ProcessConfig(limit = LOAD_LIMIT))
            }
        }
    }

    companion object {
        private const val LOAD_LIMIT = 5000
        private const val USER_EMAIL = "biostudies-dev@ebi.ac.uk"
    }
}
