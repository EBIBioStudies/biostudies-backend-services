package ebi.ac.uk.notifications.api

import ac.uk.ebi.biostd.common.properties.RtConfig
import ebi.ac.uk.notifications.exception.InvalidResponseException
import ebi.ac.uk.notifications.exception.InvalidTicketIdException
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import java.util.function.Consumer

@ExtendWith(MockKExtension::class)
class RtClientTest(
    @MockK private val client: WebClient,
    @MockK private val rtConfig: RtConfig,
    @MockK private val requestSpec: RequestBodySpec,
) {
    private val testInstance = RtClient(rtConfig, client)
    private lateinit var headerConsumer: CapturingSlot<Consumer<HttpHeaders>>
    private val createTicketBody =
        mapOf(
            "Queue" to "test-queue",
            "Subject" to "Test",
            "Status" to "resolved",
            "Requestor" to "test@mail.org",
            "AdminCc" to "admin@mail.org",
            "CustomFields" to mapOf("Accession" to "S-TEST1"),
            "Content" to "A notification",
            "ContentType" to "text/plain",
        )

    @BeforeEach
    fun beforeEach() {
        headerConsumer = slot()
        every { rtConfig.token } returns "test-token"
        every { rtConfig.queue } returns "test-queue"
        every { rtConfig.host } returns "http://test-desk"
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `create ticket`() {
        val url = "http://test-desk/REST/2.0/ticket"
        mockRtRequest(url, createTicketBody, mapOf("id" to "80338"))

        val ticketId =
            testInstance.createTicket(
                accNo = "S-TEST1",
                subject = "Test",
                owner = "test@mail.org",
                adminCc = "admin@mail.org",
                content = "A notification",
            )

        assertThat(ticketId).isEqualTo("80338")
        verifyRtRequest(url, createTicketBody)
    }

    @Test
    fun `create ticket without operational notification recipient`() {
        val url = "http://test-desk/REST/2.0/ticket"
        val requestBody = createTicketBody - "AdminCc"
        mockRtRequest(url, requestBody, mapOf("id" to "80338"))

        val ticketId =
            testInstance.createTicket(
                accNo = "S-TEST1",
                subject = "Test",
                owner = "test@mail.org",
                adminCc = null,
                content = "A notification",
            )

        assertThat(ticketId).isEqualTo("80338")
        verifyRtRequest(url, requestBody)
    }

    @Test
    fun `comment ticket sends correspondence`() {
        val url = "http://test-desk/REST/2.0/ticket/80338/correspond"
        val requestBody =
            mapOf(
                "Content" to "A comment",
                "ContentType" to "text/plain",
                "Status" to "resolved",
            )
        mockRtCorrespondenceRequest(url, requestBody)

        testInstance.commentTicket(
            ticketId = "80338",
            ccUser = "admin@mail.org",
            comment = "A comment",
        )

        verifyRtCorrespondenceRequest(url, requestBody)
    }

    @Test
    fun `comment ticket does not require operational notification recipient`() {
        val url = "http://test-desk/REST/2.0/ticket/80338/correspond"
        val requestBody =
            mapOf(
                "Content" to "A comment",
                "ContentType" to "text/plain",
                "Status" to "resolved",
            )
        mockRtCorrespondenceRequest(url, requestBody)

        testInstance.commentTicket(
            ticketId = "80338",
            ccUser = null,
            comment = "A comment",
        )

        verifyRtCorrespondenceRequest(url, requestBody)
    }

    @Test
    fun `invalid ticket response`() {
        val url = "http://test-desk/REST/2.0/ticket"
        mockRtRequest(url, createTicketBody, emptyMap())

        assertThrows<InvalidTicketIdException> {
            testInstance.createTicket(
                accNo = "S-TEST1",
                subject = "Test",
                owner = "test@mail.org",
                adminCc = "admin@mail.org",
                content = "A notification",
            )
        }
    }

    @Test
    fun `null response`() {
        val url = "http://test-desk/REST/2.0/ticket"
        mockRtRequest(url, createTicketBody, null)

        assertThrows<InvalidResponseException> {
            testInstance.createTicket(
                accNo = "S-TEST1",
                subject = "Test",
                owner = "test@mail.org",
                adminCc = "admin@mail.org",
                content = "A notification",
            )
        }
    }

    private fun mockRtRequest(
        url: String,
        requestBody: Map<String, Any>,
        response: Map<String, String>?,
    ) {
        every { client.post().uri(url) } returns requestSpec
        every { requestSpec.headers(capture(headerConsumer)) } returns requestSpec
        every { requestSpec.bodyValue(requestBody) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(Map::class.java).block() } returns response
    }

    private fun verifyRtRequest(
        url: String,
        requestBody: Map<String, Any>,
    ) {
        verify(exactly = 1) {
            client.post().uri(url)
            requestSpec.headers(any())
            requestSpec.bodyValue(requestBody)
            requestSpec.retrieve().bodyToMono(Map::class.java).block()
        }

        val headers = HttpHeaders()
        headerConsumer.captured.accept(headers)
        assertThat(headers.getFirst(AUTHORIZATION)).isEqualTo("token test-token")
        assertThat(headers.getFirst(CONTENT_TYPE)).isEqualTo(APPLICATION_JSON.toString())
    }

    private fun mockRtCorrespondenceRequest(
        url: String,
        requestBody: Map<String, Any>,
    ) {
        every { client.post().uri(url) } returns requestSpec
        every { requestSpec.headers(capture(headerConsumer)) } returns requestSpec
        every { requestSpec.bodyValue(requestBody) } returns requestSpec
        every { requestSpec.retrieve().toBodilessEntity().block() } returns ResponseEntity.ok().build<Void>()
    }

    private fun verifyRtCorrespondenceRequest(
        url: String,
        requestBody: Map<String, Any>,
    ) {
        verify(exactly = 1) {
            client.post().uri(url)
            requestSpec.headers(any())
            requestSpec.bodyValue(requestBody)
            requestSpec.retrieve().toBodilessEntity().block()
        }

        val headers = HttpHeaders()
        headerConsumer.captured.accept(headers)
        assertThat(headers.getFirst(AUTHORIZATION)).isEqualTo("token test-token")
        assertThat(headers.getFirst(CONTENT_TYPE)).isEqualTo(APPLICATION_JSON.toString())
    }
}
