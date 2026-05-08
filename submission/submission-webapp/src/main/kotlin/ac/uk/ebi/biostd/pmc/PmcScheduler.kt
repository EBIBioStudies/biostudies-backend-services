package ac.uk.ebi.biostd.pmc

import ac.uk.ebi.biostd.common.properties.PmcProperties
import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor
import ac.uk.ebi.biostd.submission.pmc.ProcessConfig
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled

private val logger = KotlinLogging.logger {}

class PmcScheduler(
    private val pmcLinksProcessor: PmcLinksProcessor,
    private val pmcProperties: PmcProperties,
) {
    // Execute every 4 hours
    @Scheduled(fixedRateString = "\${app.pmc.rateMiliseconds}", initialDelay = INITIAL_DELAY_MS)
    fun onSchedule() =
        runBlocking {
            if (pmcProperties.enableLinksExtraction) {
                logger.info { "Running scheduled PMC links loading limit = $pmcProperties.loadLimit, user = $USER_EMAIL" }
                pmcLinksProcessor.loadFromDb(ProcessConfig(limit = pmcProperties.loadLimit))
            }
        }

    companion object {
        private const val USER_EMAIL = "biostudies-dev@ebi.ac.uk"
        private const val INITIAL_DELAY_MS = 5000L
    }
}
