package ebi.ac.uk.notifications.service

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRT
import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ebi.ac.uk.notifications.api.RtClient
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class RtTicketServiceTest(
    @MockK private val notificationsDataService: NotificationsDataService,
    @MockK private val properties: NotificationProperties,
    @MockK private val rtClient: RtClient,
) {
    private lateinit var testInstance: RtTicketService

    @BeforeEach
    fun beforeEach() {
        testInstance = RtTicketService(notificationsDataService, properties, rtClient)
        every { properties.bccEmail } returns "bcc@mail.com"
    }

    @Test
    fun `saveRtTicket when ticket does not exist`() {
        val accNo = "S-BSST1"
        val subject = "Subject"
        val owner = "owner@mail.com"
        val content = "Content"
        val ticketId = "123"

        every { notificationsDataService.findTicketId(accNo) } returns null
        every { rtClient.createTicket(accNo, subject, owner, "bcc@mail.com", content) } returns ticketId
        every { notificationsDataService.saveRtNotification(accNo, ticketId) } returns mockk()

        testInstance.saveRtTicket(accNo, subject, owner, content)

        verify(exactly = 1) { rtClient.createTicket(accNo, subject, owner, "bcc@mail.com", content) }
        verify(exactly = 1) { notificationsDataService.saveRtNotification(accNo, ticketId) }
    }

    @Test
    fun `saveRtTicket when ticket exists`() {
        val accNo = "S-BSST1"
        val subject = "Subject"
        val owner = "owner@mail.com"
        val content = "Content"
        val ticketId = "123"

        every { notificationsDataService.findTicketId(accNo) } returns ticketId
        every { rtClient.commentTicket(ticketId, "bcc@mail.com", content) } returns Unit
        every { notificationsDataService.updateRtNotification(accNo) } returns mockk()

        testInstance.saveRtTicket(accNo, subject, owner, content)

        verify(exactly = 1) { rtClient.commentTicket(ticketId, "bcc@mail.com", content) }
        verify(exactly = 1) { notificationsDataService.updateRtNotification(accNo) }
    }

    private fun mockk(): SubmissionRT = io.mockk.mockk()
}
