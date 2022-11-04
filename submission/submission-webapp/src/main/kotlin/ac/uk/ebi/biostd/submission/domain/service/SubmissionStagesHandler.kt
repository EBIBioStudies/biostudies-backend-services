package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.LISTENER_FACTORY_NAME
import ac.uk.ebi.biostd.common.config.SUBMISSION_REQUEST_QUEUE
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestMessage
import ebi.ac.uk.extended.events.RequestProcessed
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

@RabbitListener(queues = [SUBMISSION_REQUEST_QUEUE], containerFactory = LISTENER_FACTORY_NAME)
class SubmissionStagesHandler(
    private val submissionSubmitter: SubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
) {
    @RabbitHandler
    fun indexRequest(rqt: RequestCreated) {
        processSafely(rqt) {
            logger.info { "$accNo, Received Created message for submission $accNo, version: $accNo" }
            submissionSubmitter.indexRequest(rqt)
            eventsPublisherService.requestIndexed(rqt.accNo, rqt.version)
        }
    }

    @RabbitHandler
    fun loadRequest(rqt: RequestIndexed) {
        processSafely(rqt) {
            logger.info { "$accNo, received Created message for submission $accNo, version: $accNo" }
            val submission = submissionSubmitter.loadRequest(rqt)
            eventsPublisherService.requestLoaded(submission.accNo, submission.version)
        }
    }

    @RabbitHandler
    fun cleanRequest(rqt: RequestLoaded) {
        processSafely(rqt) {
            logger.info { "$accNo, Received Loaded message for submission $accNo, version: $accNo" }
            submissionSubmitter.cleanRequest(rqt)
            eventsPublisherService.requestCleaned(rqt.accNo, rqt.version)
        }
    }

    @RabbitHandler
    fun processRequest(rqt: RequestCleaned) {
        processSafely(rqt) {
            logger.info { "$accNo, Received Cleaned message for submission $accNo, version: $accNo" }
            val submission = submissionSubmitter.processRequest(rqt)
            eventsPublisherService.requestProcessed(submission.accNo, submission.version)
        }
    }

    @RabbitHandler
    fun checkReleased(rqt: RequestProcessed) {
        processSafely(rqt) {
            logger.info { "$accNo, Received Processed message for submission $accNo, version: $accNo" }
            submissionSubmitter.checkReleased(rqt)
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
