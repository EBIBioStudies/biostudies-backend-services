package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ac.uk.ebi.biostd.handlers.config.FAILED_SUBMISSIONS_NOTIFICATIONS_QUEUE
import ac.uk.ebi.biostd.handlers.config.RELEASE_NOTIFICATIONS_QUEUE
import ac.uk.ebi.biostd.handlers.config.SUBMIT_NOTIFICATIONS_QUEUE
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.extended.events.FailedSubmissionRequestMessage
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.notifications.service.RtNotificationService
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener

private val logger = KotlinLogging.logger {}
internal val SYSTEM_NAME = "Submitter"
internal val HANDLERS_SUBSYSTEM = "Submission Handlers"

class SubmissionNotificationsListener(
    private val webConsumer: BioStudiesWebConsumer,
    private val notificationsSender: NotificationsSender,
    private val rtNotificationService: RtNotificationService,
    private val notificationProperties: NotificationProperties
) {
    @RabbitListener(queues = [SUBMIT_NOTIFICATIONS_QUEUE])
    fun receiveSubmissionMessage(message: SubmissionMessage) {
        logger.info { "notification for ${ message.accNo }" }
        val owner = webConsumer.getExtUser(message.extUserUrl)

        if (owner.notificationsEnabled) {
            val submission = webConsumer.getExtSubmission(message.extTabUrl)
            rtNotificationService.notifySuccessfulSubmission(submission, owner.fullName, notificationProperties.uiUrl)
        }
    }

    @RabbitListener(queues = [RELEASE_NOTIFICATIONS_QUEUE])
    fun receiveSubmissionReleaseMessage(message: SubmissionMessage) {
        logger.info { "release notification for ${ message.accNo }" }
        val owner = webConsumer.getExtUser(message.extUserUrl)

        if (owner.notificationsEnabled) {
            val submission = webConsumer.getExtSubmission(message.extTabUrl)
            rtNotificationService.notifySubmissionRelease(submission, owner.fullName, notificationProperties.uiUrl)
        }
    }

    @RabbitListener(queues = [FAILED_SUBMISSIONS_NOTIFICATIONS_QUEUE])
    fun receiveFailedSubmissionMessage(msg: FailedSubmissionRequestMessage) {
        notificationsSender.send(
            Alert(
                SYSTEM_NAME,
                HANDLERS_SUBSYSTEM,
                "Problem processing the submission '${msg.accNo}' with version ${msg.version} in mode '${msg.fileMode}'",
                msg.errorMessage
            )
        )
    }
}
