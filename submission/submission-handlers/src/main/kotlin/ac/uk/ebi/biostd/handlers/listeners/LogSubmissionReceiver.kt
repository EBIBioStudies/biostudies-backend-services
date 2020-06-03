package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.LOG_QUEUE
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener

private val logger = KotlinLogging.logger {}

class LogSubmissionReceiver {

    @RabbitListener(queues = [LOG_QUEUE])
    fun receiveMessage(message: String) {
        logger.info { "received message $message" }
    }
}
