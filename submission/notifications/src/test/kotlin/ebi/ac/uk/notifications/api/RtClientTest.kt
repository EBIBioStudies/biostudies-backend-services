package ebi.ac.uk.notifications.api

import ac.uk.ebi.biostd.common.properties.RtConfig
import ebi.ac.uk.notifications.exception.InvalidResponseException
import ebi.ac.uk.notifications.exception.InvalidTicketIdException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

class RtClientTest {
    private val rtConfig = mockk<RtConfig>()
    private val restTemplate = mockk<RestTemplate>()
    private val testInstance = RtClient(rtConfig, restTemplate)
    private val testBody = LinkedMultiValueMap<String, String>(
        mapOf(
            "content" to listOf(
                "Queue: test-queue\nSubject: Test\nRequestor: test@mail.org\nCF-Accession: S-TEST1\nText: A notification"
            )
        )
    )

    @BeforeEach
    fun beforeEach() {
        every { rtConfig.user } returns "test-user"
        every { rtConfig.password } returns "123456"
        every { rtConfig.queue } returns "test-queue"
        every { rtConfig.host } returns "http://test-desk"
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `create ticket`() {
        val response = "RT/4.2.16 200 Ok\n\n# Ticket 80338 created.\n\n"
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=123456"

        every { restTemplate.postForEntity<String>(url, testBody) } returns ResponseEntity(response, ACCEPTED)

        val ticketId = testInstance.createTicket("S-TEST1", "Test", "test@mail.org", "A notification")
        assertThat(ticketId).isEqualTo("80338")
    }

    @Test
    fun `comment ticket`() {
        val url = "http://test-desk/REST/1.0/ticket/80338/comment?user=test-user&pass=123456"
        val testBody = LinkedMultiValueMap<String, String>(
            mapOf("content" to listOf("id: 80338\nAction: correspond\nText: A comment"))
        )

        every { restTemplate.postForEntity<String>(url, testBody) } returns ResponseEntity("response", ACCEPTED)

        testInstance.commentTicket("80338", "A comment")

        verify(exactly = 1) { restTemplate.postForEntity<String>(url, testBody) }
    }

    @Test
    fun `invalid response`() {
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=123"

        every { rtConfig.password } returns "123"
        every {
            restTemplate.postForEntity<String>(url, testBody)
        } returns ResponseEntity("Wrong user/password", UNAUTHORIZED)

        assertThrows<InvalidTicketIdException> {
            testInstance.createTicket("S-TEST1", "Test", "test@mail.org", "A notification")
        }
    }

    @Test
    fun `bad request`() {
        val response = "RT/4.2.16 400 Ok\n\n# Queue not set.\n\n"
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=1234"

        every { rtConfig.password } returns "1234"
        every { restTemplate.postForEntity<String>(url, testBody) } returns ResponseEntity(response, BAD_REQUEST)

        assertThrows<InvalidTicketIdException> {
            testInstance.createTicket("S-TEST1", "Test", "test@mail.org", "A notification")
        }
    }

    @Test
    fun `server down`() {
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=12"

        every { rtConfig.password } returns "12"
        every { restTemplate.postForEntity<String>(url, testBody) } returns ResponseEntity(GATEWAY_TIMEOUT)

        assertThrows<InvalidResponseException> {
            testInstance.createTicket("S-TEST1", "Test", "test@mail.org", "A notification")
        }
    }
}
