package ebi.ac.uk.notifications.api

import ebi.ac.uk.notifications.integration.RtConfig
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate

class RtClientTest {
    private val rtConfig = mockk<RtConfig>()
    private val restTemplate = mockk<RestTemplate>()
    private val testInstance = RtClient(rtConfig, restTemplate)

    @Test
    fun `get ticket id`() {
        val response = "RT/4.2.16 200 Ok\n\n# Ticket 80338 created.\n\n"
        val ticketId = testInstance.getTicketId(response)

        assertThat(ticketId).isEqualTo("80338")
    }
}
