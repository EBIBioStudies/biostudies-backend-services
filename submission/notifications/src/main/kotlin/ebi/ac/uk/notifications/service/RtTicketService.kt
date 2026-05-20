package ebi.ac.uk.notifications.service

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ebi.ac.uk.notifications.api.RtClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class RtTicketService(
    private val notificationsDataService: NotificationsDataService,
    private val properties: NotificationProperties,
    private val rtClient: RtClient,
) {
    fun saveRtTicket(
        accNo: String,
        subject: String,
        owner: String,
        content: String,
    ) = when (val ticketId = notificationsDataService.findTicketId(accNo)) {
        null -> createTicket(accNo, subject, owner, content)
        else -> commentTicket(accNo, ticketId, content)
    }

    private fun commentTicket(
        accNo: String,
        ticketId: String,
        content: String,
    ) {
        logger.info { "Commenting RT ticket $ticketId with content: '$content'" }
        rtClient.commentTicket(ticketId, properties.bccEmail, content)
        notificationsDataService.updateRtNotification(accNo)
    }

    private fun createTicket(
        accNo: String,
        subject: String,
        owner: String,
        content: String,
    ) {
        logger.info { "Creating RT ticket for submission $accNo with subject '$subject' and owner '$owner'" }
        val ticketId =
            rtClient.createTicket(
                accNo = accNo,
                subject = subject,
                owner = owner,
                adminCc = properties.bccEmail,
                content = content,
            )
        logger.info { "RT ticket created with ID: $ticketId for submission $accNo" }
        notificationsDataService.saveRtNotification(accNo, ticketId)
    }
}
