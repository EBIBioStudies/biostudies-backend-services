package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.LISTENER_FACTORY_NAME
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import ebi.ac.uk.extended.events.RequestCheckedReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestFinalized
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestMessage
import ebi.ac.uk.extended.events.RequestPersisted
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

@RabbitListener(queues = ["\${app.notifications.requestQueue}"], containerFactory = LISTENER_FACTORY_NAME)
class SubmissionMessageListener(
    private val statsService: SubmissionStatsService,
    private val submissionSubmitter: ExtSubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
) {
    @RabbitHandler
    fun indexRequest(rqt: RequestCreated) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            logger.info { "$accNo, Received index message for submission $accNo, version: $version" }
            submissionSubmitter.indexRequest(accNo, version)
        }
    }

    @RabbitHandler
    fun loadRequest(rqt: RequestIndexed) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            logger.info { "$accNo, Received load message for submission $accNo, version: $version" }
            submissionSubmitter.loadRequest(accNo, version)
        }
    }

    @RabbitHandler
    fun cleanRequest(rqt: RequestLoaded) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            logger.info { "$accNo, Received clean message for submission $accNo, version: $version" }
            submissionSubmitter.cleanRequest(accNo, version)
        }
    }

    @RabbitHandler
    fun copyRequestFiles(rqt: RequestCleaned) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            logger.info { "$accNo, Received persist files message for submission $accNo, version: $version" }
            submissionSubmitter.processRequest(accNo, version)
        }
    }

    @RabbitHandler
    fun checkReleased(rqt: RequestFilesCopied) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            logger.info { "$accNo, Received check release status message for submission $accNo, version: $version" }
            submissionSubmitter.checkReleased(accNo, version)
        }
    }

    @RabbitHandler
    fun saveSubmission(rqt: RequestCheckedReleased) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            logger.info { "$accNo, Received save submission message for submission $accNo, version: $version" }
            submissionSubmitter.saveRequest(accNo, version)
        }
    }

    @RabbitHandler
    fun finalizeRequest(rqt: RequestPersisted) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            logger.info { "$accNo, Received finalize submission message for submission $accNo, version: $version" }
            submissionSubmitter.finalizeRequest(accNo, version)
        }
    }

    @RabbitHandler
    fun calculateStats(rqt: RequestFinalized) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            logger.info { "$accNo, Received calculate stats message for submission $accNo, version: $version" }
            statsService.calculateSubFilesSize(rqt.accNo)
        }
    }

    private fun processSafely(request: RequestMessage, process: suspend RequestMessage.() -> Unit) = runBlocking {
        runCatching { process(request) }.onFailure { onError(it, request) }
    }

    private fun onError(exception: Throwable, rqt: RequestMessage) {
        logger.error(exception) { "${rqt.accNo}, Problem processing request '${rqt.accNo}': ${exception.message}" }
        eventsPublisherService.submissionFailed(rqt)
    }
}
