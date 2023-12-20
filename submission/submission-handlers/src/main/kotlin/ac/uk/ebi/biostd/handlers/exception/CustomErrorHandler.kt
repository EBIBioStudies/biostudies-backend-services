package ac.uk.ebi.biostd.handlers.exception

import ac.uk.ebi.biostd.handlers.common.HANDLERS_SUBSYSTEM
import ac.uk.ebi.biostd.handlers.common.SYSTEM_NAME
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import kotlinx.coroutines.runBlocking
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler

class CustomErrorHandler(
    private val notificationsSender: NotificationsSender,
) : ConditionalRejectingErrorHandler() {
    override fun handleError(t: Throwable) {
        runBlocking {
            notificationsSender.send(Alert(SYSTEM_NAME, HANDLERS_SUBSYSTEM, "$ERROR_MESSAGE: ${t.localizedMessage}"))
        }

        throw AmqpRejectAndDontRequeueException(t)
    }

    companion object {
        internal const val ERROR_MESSAGE = "Problem processing rabbit message for notification"
    }
}
