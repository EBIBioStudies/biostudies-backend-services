package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.config.LOG_QUEUE
import ebi.ac.uk.extended.events.SubmissionMessage
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener

private val logger = KotlinLogging.logger {}

class LogSubmissionListener {
    @RabbitListener(queues = [LOG_QUEUE])
    fun receiveMessage(submission: SubmissionMessage) {
        logger.info { "received message for submission ${submission.accNo}" }
    }
}
