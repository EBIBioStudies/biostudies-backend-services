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
    fun onStart() = retryStuckSubmissions("application start")

    @Scheduled(cron = "0 0 */3 * * ?")
    fun onSchedule() = retryStuckSubmissions("scheduled")

    private fun retryStuckSubmissions(trigger: String) =
        runBlocking {
            var candidates = 0
            var retried = 0
            var locked = 0

            logger.info { "Starting retry run[trigger='$trigger']" }
            requestService
                .getProcessingRequests(RETRY_THRESHOLD)
                .chunked(RETRY_BATCH)
                .collect { batch ->
                    val activeLocks = requestService.getActiveSubmissionLocks(batch)
                    val retryable = batch.filterNot(activeLocks::contains)

                    candidates += batch.size
                    locked += activeLocks.size
                    retried += retryable.size
                    if (retryable.isNotEmpty()) reTriggerSafely(retryable)
                }

            logger.info {
                "Finished retry run[trigger='$trigger', candidates='$candidates', retried='$retried', locked='$locked']"
            }
        }

    private suspend fun reTriggerSafely(submissionIds: List<SubmissionId>) {
        runCatching { extSubmissionService.reTriggerSubmissionAsync(submissionIds) }
            .onFailure { logger.error(it) { "Failed to re-trigger submission batch[size='${submissionIds.size}']" } }
    }

    private companion object {
        const val RETRY_BATCH = 500
        val RETRY_THRESHOLD: Duration = Duration.ofHours(3)
    }
}
