package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ebi.ac.uk.coroutines.chunked
import ebi.ac.uk.model.SubmissionId
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
    fun onStart() =
        runBlocking {
            logger.info { "Re processing pending submission on application start" }
            requestService
                .getActiveRequests(Duration.of(3, ChronoUnit.HOURS))
                .chunked(RETRY_BATCH)
                .collect { reTriggerSafely(it.map { (accNo, version) -> SubmissionId(accNo, version) }) }
        }

    @Scheduled(cron = "0 0 */3 * * ?")
    fun onSchedule() =
        runBlocking {
            logger.info { "Scheduled re processing of pending submission" }
            requestService
                .getActiveRequests(Duration.of(3, ChronoUnit.HOURS))
                .chunked(RETRY_BATCH)
                .collect { reTriggerSafely(it.map { (accNo, version) -> SubmissionId(accNo, version) }) }
        }

    private suspend fun reTriggerSafely(submissionIds: List<SubmissionId>) {
        runCatching { extSubmissionService.reTriggerSubmissionAsync(submissionIds) }
            .onFailure { logger.error { "Failed to re triggering submission batch[size='${submissionIds.size}']" } }
            .onSuccess { logger.info { "Completed processing of request batch[size='${submissionIds.size}']" } }
    }

    private companion object {
        const val RETRY_BATCH = 500
    }
}
