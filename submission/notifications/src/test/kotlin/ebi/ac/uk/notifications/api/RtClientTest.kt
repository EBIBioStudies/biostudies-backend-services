package ebi.ac.uk.notifications.api

import ebi.ac.uk.notifications.exception.InvalidResponseException
import ebi.ac.uk.notifications.exception.InvalidTicketIdException
import ebi.ac.uk.notifications.integration.RtConfig
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

class RtClientTest {
    private val rtConfig = mockk<RtConfig>()
    private val restTemplate = mockk<RestTemplate>()
    private val testInstance = RtClient(rtConfig, restTemplate)
    private val testBody = LinkedMultiValueMap<String, String>(
        mapOf("content" to listOf(
            "Queue: test-queue\nSubject: Test\nOwner: test@mail.org\nText: A notification\n\n ")))

    @BeforeEach
    fun beforeEach() {
        every { rtConfig.user } returns "test-user"
        every { rtConfig.password } returns "123456"
        every { rtConfig.queue } returns "test-queue"
        every { rtConfig.host } returns "http://test-desk"
    }

    @Test
    fun `successful notification`() {
        val response = "RT/4.2.16 200 Ok\n\n# Ticket 80338 created.\n\n"
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=123456"
        every {
            restTemplate.postForEntity<String>(url, testBody)
        } returns ResponseEntity(response, HttpStatus.ACCEPTED)

        val ticketId = testInstance.createTicket("Test", "test@mail.org", "A notification")
        assertThat(ticketId).isEqualTo("80338")
    }

    @Test
    fun `invalid response`() {
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=123"

        every { rtConfig.password } returns "123"
        every {
            restTemplate.postForEntity<String>(url, testBody)
        } returns ResponseEntity("Wrong user/password", HttpStatus.UNAUTHORIZED)

        assertThrows<InvalidTicketIdException> { testInstance.createTicket("Test", "test@mail.org", "A notification") }
    }

    @Test
    fun `bad request`() {
        val response = "RT/4.2.16 400 Ok\n\n# Queue not set.\n\n"
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=1234"

        every { rtConfig.password } returns "1234"
        every {
            restTemplate.postForEntity<String>(url, testBody)
        } returns ResponseEntity(response, HttpStatus.BAD_REQUEST)

        assertThrows<InvalidTicketIdException> { testInstance.createTicket("Test", "test@mail.org", "A notification") }
    }

    @Test
    fun `server down`() {
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=12"

        every { rtConfig.password } returns "12"
        every {
            restTemplate.postForEntity<String>(url, testBody)
        } returns ResponseEntity(HttpStatus.GATEWAY_TIMEOUT)

        assertThrows<InvalidResponseException> { testInstance.createTicket("Test", "test@mail.org", "A notification") }
    }
}
