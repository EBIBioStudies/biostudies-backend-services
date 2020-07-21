package ebi.ac.uk.notifications.persistence.service

import ebi.ac.uk.notifications.persistence.model.SubmissionRT
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class NotificationPersistenceServiceTest(@MockK private val submissionRtRepository: SubmissionRtRepository) {
    private val testInstance = NotificationPersistenceService(submissionRtRepository)

    @Test
    fun `save notification`() {
        val ticketSlot = slot<SubmissionRT>()
        val ticket = SubmissionRT("S-TEST123", "78910")

        every { submissionRtRepository.save(capture(ticketSlot)) } returns ticket

        testInstance.saveRtNotification("S-TEST123", "78910")

        assertThat(ticketSlot.captured.accNo).isEqualTo("S-TEST123")
        assertThat(ticketSlot.captured.ticketId).isEqualTo("78910")
    }

    @Test
    fun `find ticket`() {
        val ticket = SubmissionRT("S-TEST123", "78910")
        every { submissionRtRepository.findByAccNo("S-TEST123") } returns ticket

        val ticketId = testInstance.findTicketId("S-TEST123")
        assertThat(ticketId).isEqualTo("78910")
    }

    @Test
    fun `ticket not existing`() {
        every { submissionRtRepository.findByAccNo("S-TEST123") } returns null
        assertThat(testInstance.findTicketId("S-TEST123")).isNull()
    }
}
