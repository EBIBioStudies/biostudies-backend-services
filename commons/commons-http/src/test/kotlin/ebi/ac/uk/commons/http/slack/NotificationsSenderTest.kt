package ebi.ac.uk.commons.http.slack

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.util.function.Consumer

@ExtendWith(MockKExtension::class)
class NotificationsSenderTest(
    @MockK private val client: WebClient,
    @MockK private val requestSpec: RequestBodySpec,
) {
    private val testInstance = NotificationsSender(client, "http://notifications:8080")

    @Test
    fun send() =
        runTest {
            val bodySlot = slot<Notification>()
            val headersSlot = slot<Consumer<HttpHeaders>>()
            val notification = Report("system", "subsystem", "result")

            every { client.post().uri("http://notifications:8080") } returns requestSpec
            every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
            every { requestSpec.headers(capture(headersSlot)) } returns requestSpec
            every { requestSpec.retrieve().bodyToMono<String>() } returns Mono.just("")

            testInstance.send(notification)

            val body = bodySlot.captured
            val headers = headersSlot.captured
            assertThat(body).isEqualTo(notification.asNotification())
            headers.andThen {
                assertThat(it[ACCEPT]!!.first()).isEqualTo(APPLICATION_JSON)
                assertThat(it[CONTENT_TYPE]!!.first()).isEqualTo(APPLICATION_JSON)
            }
            verify(exactly = 1) {
                client.post().uri("http://notifications:8080")
                requestSpec.bodyValue(body)
                requestSpec.retrieve().bodyToMono<String>()
            }
        }
}
