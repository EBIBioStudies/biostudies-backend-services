package ebi.ac.uk.commons.http.slack

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.util.Optional

@ExtendWith(MockKExtension::class)
class NotificationsSenderTest(
    @MockK private val restTemplate: RestTemplate
) {
    private val testInstance = NotificationsSender(restTemplate, "http://notifications:8080")

    @Test
    fun send() {
        val requestSlot = slot<HttpEntity<Notification>>()
        val notification = Report("system", "subsystem", "result")

        every {
            restTemplate.postForEntity<String>("http://notifications:8080", capture(requestSlot))
        } returns ResponseEntity.of(Optional.of(""))

        testInstance.send(notification)

        val request = requestSlot.captured
        assertThat(request.headers.contentType).isEqualTo(APPLICATION_JSON)
        assertThat(request.headers.accept).containsExactly(APPLICATION_JSON)
        assertThat(request.body).isEqualTo(notification.asNotification())
        verify(exactly = 1) { restTemplate.postForEntity<String>("http://notifications:8080", request) }
    }
}
