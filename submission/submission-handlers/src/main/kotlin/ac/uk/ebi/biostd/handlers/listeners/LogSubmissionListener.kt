package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.config.LOG_QUEUE
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import uk.ac.ebi.biostd.extended.events.SubmissionMessage

private val logger = KotlinLogging.logger {}

class LogSubmissionListener {
    @RabbitListener(queues = [LOG_QUEUE])
    fun receiveMessage(submission: SubmissionMessage) {
        logger.info { "Received message for submission ${submission.accNo}" }
    }
}
