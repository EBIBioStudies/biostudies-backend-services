package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.LISTENER_FACTORY_NAME
import ac.uk.ebi.biostd.submission.submitter.SubmissionStagesHandler
import ebi.ac.uk.extended.events.RequestCheckedReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestFinalized
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
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
        stagesHandler.indexRequest(rqt)
    }

    @RabbitHandler
    fun loadRequest(rqt: RequestIndexed) {
        stagesHandler.loadRequest(rqt)
    }

    @RabbitHandler
    fun cleanRequest(rqt: RequestLoaded) {
        stagesHandler.cleanRequest(rqt)
    }

    @RabbitHandler
    fun copyRequestFiles(rqt: RequestCleaned) {
        stagesHandler.copyRequestFiles(rqt)
    }

    @RabbitHandler
    fun checkReleased(rqt: RequestFilesCopied) {
        stagesHandler.checkReleased(rqt)
    }

    @RabbitHandler
    fun saveSubmission(rqt: RequestCheckedReleased) {
        stagesHandler.saveSubmission(rqt)
    }

    @RabbitHandler
    fun finalizeRequest(rqt: RequestPersisted) {
        stagesHandler.finalizeRequest(rqt)
    }

    @RabbitHandler
    fun calculateStats(rqt: RequestFinalized) {
        stagesHandler.calculateStats(rqt)
    }
}
