package ebi.ac.uk.notifications.service

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ebi.ac.uk.notifications.api.RtClient

class RtTicketService(
    private val notificationsDataService: NotificationsDataService,
    private val properties: NotificationProperties,
    private val rtClient: RtClient,
) {
    fun saveRtTicket(accNo: String, subject: String, owner: String, content: String) =
        when (val ticketId = notificationsDataService.findTicketId(accNo)) {
            null -> createTicket(accNo, subject, owner, content)
            else -> rtClient.commentTicket(ticketId, content)
        }

    private fun createTicket(accNo: String, subject: String, owner: String, content: String) {
        val ticketId = rtClient.createTicket(
            accNo = accNo,
            subject = subject,
            owner = owner,
            adminCc = properties.bccEmail,
            content = content
        )
        notificationsDataService.saveRtNotification(accNo, ticketId)
    }
}
