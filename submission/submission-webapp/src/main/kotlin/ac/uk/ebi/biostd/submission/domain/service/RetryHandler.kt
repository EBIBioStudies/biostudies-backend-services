package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import java.time.Duration
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

/**
 * Re trigger pending per processing request on application start.
 */
@Suppress("MagicNumber")
class RetryHandler(
    private val extSubmissionService: ExtSubmissionService,
    private val requestService: SubmissionRequestPersistenceService,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun onStart() = runBlocking {
        logger.info { "Re processing pending submission on application start" }
        requestService.getProcessingRequests(Duration.of(3, ChronoUnit.HOURS))
            .collect { (accNo, version) -> reTriggerSafely(accNo, version) }
    }

    @Scheduled(cron = "0 0 */3 * * ?")
    fun onSchedule() = runBlocking {
        logger.info { "Scheduled re processing of pending submission" }
        requestService.getProcessingRequests(Duration.of(3, ChronoUnit.HOURS))
            .collect { (accNo, version) -> reTriggerSafely(accNo, version) }
    }

    private fun reTriggerSafely(accNo: String, version: Int) {
        runCatching { runBlocking { extSubmissionService.reTriggerSubmission(accNo, version) } }
            .onFailure { logger.error { "Failed to re triggering request accNo='$accNo', version='$version'" } }
            .onSuccess { logger.info { "Completed processing of request accNo='$accNo', version='$version'" } }
    }
}
