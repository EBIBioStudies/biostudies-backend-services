package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.LISTENER_FACTORY_NAME
import ac.uk.ebi.biostd.submission.domain.request.SubmissionStagesHandler
import ebi.ac.uk.extended.events.RequestCheckedReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestFinalized
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestPageTabGenerated
import ebi.ac.uk.extended.events.RequestPersisted
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener

private val logger = KotlinLogging.logger {}

@RabbitListener(queues = ["\${app.notifications.requestQueue}"], containerFactory = LISTENER_FACTORY_NAME)
class SubmissionMessageListener(
    private val stagesHandler: SubmissionStagesHandler,
) {
    @RabbitHandler
    fun indexRequest(rqt: RequestCreated) {
        val (accNo, version) = rqt
        logger.info { "$accNo, Received index message for submission $accNo, version: $version" }
        stagesHandler.indexRequest(rqt)
    }

    @RabbitHandler
    fun loadRequest(rqt: RequestIndexed) {
        val (accNo, version) = rqt
        logger.info { "$accNo, Received load message for submission $accNo, version: $version" }
        stagesHandler.loadRequest(rqt)
    }

    @RabbitHandler
    fun generatePageTabRequest(rqt: RequestLoaded) {
        val (accNo, version) = rqt
        logger.info { "$accNo, Received generate pagetab message for submission $accNo, version: $version" }
        stagesHandler.generatePageTabRequest(rqt)
    }

    @RabbitHandler
    fun cleanRequest(rqt: RequestPageTabGenerated) {
        val (accNo, version) = rqt
        logger.info { "$accNo, Received clean message for submission $accNo, version: $version" }
        stagesHandler.cleanRequest(rqt)
    }

    @RabbitHandler
    fun copyRequestFiles(rqt: RequestCleaned) {
        val (accNo, version) = rqt
        logger.info { "$accNo, Received persist files message for submission $accNo, version: $version" }
        stagesHandler.copyRequestFiles(rqt)
    }

    @RabbitHandler
    fun checkReleased(rqt: RequestFilesCopied) {
        val (accNo, version) = rqt
        logger.info { "$accNo, Received check release status message for submission $accNo, version: $version" }
        stagesHandler.checkReleased(rqt)
    }

    @RabbitHandler
    fun saveSubmission(rqt: RequestCheckedReleased) {
        val (accNo, version) = rqt
        logger.info { "$accNo, Received save submission message for submission $accNo, version: $version" }
        stagesHandler.saveSubmission(rqt)
    }

    @RabbitHandler
    fun finalizeRequest(rqt: RequestPersisted) {
        val (accNo, version) = rqt
        logger.info { "$accNo, Received finalize submission message for submission $accNo, version: $version" }
        stagesHandler.finalizeRequest(rqt)
    }

    @RabbitHandler
    fun calculateStats(rqt: RequestFinalized) {
        val (accNo, version) = rqt
        logger.info { "$accNo, Received calculate status message for submission $accNo, version: $version" }
        stagesHandler.calculateStats(rqt)
    }
}
