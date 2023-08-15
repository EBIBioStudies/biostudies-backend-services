package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.LISTENER_FACTORY_NAME
import ac.uk.ebi.biostd.stats.domain.service.SubmissionStatsService
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.RequestCheckedReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestFinalized
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestMessage
import ebi.ac.uk.extended.events.RequestPersisted
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

@RabbitListener(queues = ["\${app.notifications.requestQueue}"], containerFactory = LISTENER_FACTORY_NAME)
class SubmissionStagesHandler(
    private val statsService: SubmissionStatsService,
    private val submissionSubmitter: SubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
) {
    @RabbitHandler
    fun indexRequest(rqt: RequestCreated) {
        processSafely(rqt) {
            logger.info { "$accNo, Received Created message for submission $accNo, version: $version" }
            submissionSubmitter.indexRequest(rqt)
            eventsPublisherService.requestIndexed(rqt.accNo, rqt.version)
        }
    }

    @RabbitHandler
    fun loadRequest(rqt: RequestIndexed) {
        processSafely(rqt) {
            logger.info { "$accNo, Received Created message for submission $accNo, version: $version" }
            submissionSubmitter.loadRequest(rqt)
            eventsPublisherService.requestLoaded(rqt.accNo, rqt.version)
        }
    }

    @RabbitHandler
    fun cleanRequest(rqt: RequestLoaded) {
        processSafely(rqt) {
            logger.info { "$accNo, Received Loaded message for submission $accNo, version: $version" }
            submissionSubmitter.cleanRequest(rqt)
            eventsPublisherService.requestCleaned(rqt.accNo, rqt.version)
        }
    }

    @RabbitHandler
    fun copyRequestFiles(rqt: RequestCleaned) {
        processSafely(rqt) {
            logger.info { "$accNo, Received Cleaned message for submission $accNo, version: $version" }
            submissionSubmitter.processRequest(rqt)
            eventsPublisherService.requestFilesCopied(rqt.accNo, rqt.version)
        }
    }

    @RabbitHandler
    fun checkReleased(rqt: RequestFilesCopied) {
        processSafely(rqt) {
            logger.info { "$accNo, Received Processed message for submission $accNo, version: $version" }
            submissionSubmitter.checkReleased(rqt)
            eventsPublisherService.checkReleased(rqt.accNo, rqt.version)
        }
    }

    @RabbitHandler
    fun saveSubmission(rqt: RequestCheckedReleased) {
        processSafely(rqt) {
            logger.info { "$accNo, Received check released message for submission $accNo, version: $version" }
            submissionSubmitter.saveRequest(rqt)
            eventsPublisherService.submissionPersisted(rqt.accNo, rqt.version)
        }
    }

    @RabbitHandler
    fun finalizeRequest(rqt: RequestPersisted) {
        processSafely(rqt) {
            logger.info { "$accNo, Received processed message for submission $accNo, version: $version" }
            submissionSubmitter.finalizeRequest(rqt)
        }
    }

    @RabbitHandler
    fun calculateStats(rqt: RequestFinalized) {
        processSafely(rqt) {
            logger.info { "$accNo, Received finalized message for submission $accNo, version: $version" }
            statsService.calculateSubFilesSize(accNo)
        }
    }

    private fun processSafely(request: RequestMessage, process: RequestMessage.() -> Unit) {
        runCatching { process(request) }.onFailure { onError(it, request) }
    }

    private fun onError(exception: Throwable, rqt: RequestMessage) {
        logger.error(exception) { "${rqt.accNo}, Problem processing request '${rqt.accNo}': ${exception.message}" }
        eventsPublisherService.submissionFailed(rqt)
    }
}
