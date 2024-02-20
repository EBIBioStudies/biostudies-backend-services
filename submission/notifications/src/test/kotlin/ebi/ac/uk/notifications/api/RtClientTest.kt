package ebi.ac.uk.notifications.api

import ac.uk.ebi.biostd.common.properties.RtConfig
import ebi.ac.uk.notifications.exception.InvalidResponseException
import ebi.ac.uk.notifications.exception.InvalidTicketIdException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec

@ExtendWith(MockKExtension::class)
class RtClientTest(
    @MockK private val client: WebClient,
    @MockK private val rtConfig: RtConfig,
    @MockK private val requestSpec: RequestBodySpec,
) {
    private val testInstance = RtClient(rtConfig, client)
    private val testBody = LinkedMultiValueMap(
        mapOf(
            "content" to listOf(
                buildString {
                    appendLine("Queue: test-queue")
                    appendLine("Subject: Test")
                    appendLine("Status: resolved")
                    appendLine("Requestor: test@mail.org")
                    appendLine("AdminCc: admin@mail.org")
                    appendLine("CF-Accession: S-TEST1")
                    append("Text: A notification")
                }
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

        every { client.post().uri(url) } returns requestSpec
        every { requestSpec.bodyValue(testBody) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns response

        val ticketId = testInstance.createTicket(
            accNo = "S-TEST1",
            subject = "Test",
            owner = "test@mail.org",
            adminCc = "admin@mail.org",
            content = "A notification"
        )

        assertThat(ticketId).isEqualTo("80338")
    }

    @Test
    fun `comment ticket`() {
        val url = "http://test-desk/REST/1.0/ticket/80338/comment?user=test-user&pass=123456"
        val testBody = LinkedMultiValueMap(
            mapOf("content" to listOf("id: 80338\nAction: correspond\nStatus: resolved\nText: A comment"))
        )

        every { client.post().uri(url) } returns requestSpec
        every { requestSpec.bodyValue(testBody) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns "response"

        testInstance.commentTicket("80338", "A comment")

        verify(exactly = 1) {
            client.post().uri(url)
            requestSpec.bodyValue(testBody)
            requestSpec.retrieve().bodyToMono(String::class.java).block()
        }
    }

    @Test
    fun `invalid response`() {
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=123"

        every { rtConfig.password } returns "123"
        every { client.post().uri(url) } returns requestSpec
        every { requestSpec.bodyValue(testBody) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns "Wrong user/password"

        assertThrows<InvalidTicketIdException> {
            testInstance.createTicket(
                accNo = "S-TEST1",
                subject = "Test",
                owner = "test@mail.org",
                adminCc = "admin@mail.org",
                content = "A notification"
            )
        }
    }

    @Test
    fun `bad request`() {
        val response = "RT/4.2.16 400 Ok\n\n# Queue not set.\n\n"
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=1234"

        every { rtConfig.password } returns "1234"
        every { client.post().uri(url) } returns requestSpec
        every { requestSpec.bodyValue(testBody) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns response

        assertThrows<InvalidTicketIdException> {
            testInstance.createTicket(
                accNo = "S-TEST1",
                subject = "Test",
                owner = "test@mail.org",
                adminCc = "admin@mail.org",
                content = "A notification"
            )
        }
    }

    @Test
    fun `server down`() {
        val url = "http://test-desk/REST/1.0/ticket/new?user=test-user&pass=12"

        every { rtConfig.password } returns "12"
        every { client.post().uri(url) } returns requestSpec
        every { requestSpec.bodyValue(testBody) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns null

        assertThrows<InvalidResponseException> {
            testInstance.createTicket(
                accNo = "S-TEST1",
                subject = "Test",
                owner = "test@mail.org",
                adminCc = "admin@mail.org",
                content = "A notification"
            )
        }
    }
}
