package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

private val logger = KotlinLogging.logger {}

/**
 * Re trigger pending per processing request on application start.
 */
class StartApplicationHandler(
    private val extSubmissionService: ExtSubmissionService,
    private val requestService: SubmissionRequestPersistenceService,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun onStart() {
        logger.info { "Re processing pending submission" }
        requestService.getProcessingRequests()
            .forEach { (accNo, version) -> reTriggerSafely(accNo, version) }
    }

    private fun reTriggerSafely(accNo: String, version: Int) {
        runCatching { extSubmissionService.reTriggerSubmission(accNo, version) }
            .onFailure { logger.error { "Failed to re triggering request accNo='$accNo', version='$version'" } }
            .onSuccess { logger.info { "Re triggering request accNo='$accNo', version='$version'" } }
    }
}
