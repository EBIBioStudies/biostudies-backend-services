package uk.ac.ebi.events.service

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.events.SECURITY_NOTIFICATIONS_ROUTING_KEY
import ac.uk.ebi.biostd.common.events.SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY
import ac.uk.ebi.biostd.common.events.SUBMISSIONS_ROUTING_KEY
import ac.uk.ebi.biostd.common.properties.NotificationsProperties
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestFinalized
import ebi.ac.uk.extended.events.RequestMessage
import ebi.ac.uk.extended.events.RequestPersisted
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.util.date.asIsoTime
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.EventsProperties
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class EventsPublisherServiceTest(
    @MockK private val rabbitTemplate: RabbitTemplate,
    @MockK private val eventsProperties: EventsProperties,
    @MockK private val notificationsProperties: NotificationsProperties,
) {
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 1, 2, 3, 4, ZoneOffset.UTC)
    private val testInstance = EventsPublisherService(rabbitTemplate, eventsProperties, notificationsProperties)

    @BeforeEach
    fun beforeEach() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns mockNow
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun securityNotification(
        @MockK notification: SecurityNotification,
    ) {
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SECURITY_NOTIFICATIONS_ROUTING_KEY, notification)
        } answers { nothing }

        testInstance.securityNotification(notification)

        verify(exactly = 1) {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SECURITY_NOTIFICATIONS_ROUTING_KEY, notification)
        }
    }

    @Test
    fun submissionSubmitted() {
        val notificationSlot = slot<SubmissionMessage>()

        every { eventsProperties.instanceBaseUrl } returns "http://biostudies:8788"
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, capture(notificationSlot))
        } answers { nothing }

        testInstance.submissionSubmitted("S-BSST0", "test@ebi.ac.uk")

        val notification = notificationSlot.captured
        assertThat(notification.accNo).isEqualTo("S-BSST0")
        assertThat(notification.eventTime).isEqualTo(mockNow.asIsoTime())
        assertThat(notification.pagetabUrl).isEqualTo("http://biostudies:8788/submissions/S-BSST0.json")
        assertThat(notification.extTabUrl).isEqualTo("http://biostudies:8788/submissions/extended/S-BSST0")
        assertThat(notification.extUserUrl).isEqualTo("http://biostudies:8788/security/users/extended/test@ebi.ac.uk")

        verify(exactly = 1) {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, notification)
        }
    }

    @Test
    fun submissionFailed(
        @MockK request: RequestMessage,
    ) {
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY, request)
        } answers { nothing }

        testInstance.submissionFailed(request)

        verify(exactly = 1) {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY, request)
        }
    }

    @Test
    fun `request cleaned`() {
        val requestSlot = slot<RequestCleaned>()
        every { notificationsProperties.requestRoutingKey } returns KEY
        every { rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, KEY, capture(requestSlot)) } answers { nothing }

        testInstance.requestCleaned("S-BSST0", 1)

        val request = requestSlot.captured
        assertThat(request.version).isEqualTo(1)
        assertThat(request.accNo).isEqualTo("S-BSST0")
        verify(exactly = 1) { rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, KEY, request) }
    }

    @Test
    fun `request persisted`() {
        val requestSlot = slot<RequestPersisted>()
        every { notificationsProperties.requestRoutingKey } returns KEY
        every { rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, KEY, capture(requestSlot)) } answers { nothing }

        testInstance.submissionPersisted("S-BSST0", 1)

        val request = requestSlot.captured
        assertThat(request.version).isEqualTo(1)
        assertThat(request.accNo).isEqualTo("S-BSST0")
        verify(exactly = 1) { rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, KEY, request) }
    }

    @Test
    fun `request finalized`() {
        val requestSlot = slot<RequestFinalized>()
        every { notificationsProperties.requestRoutingKey } returns KEY
        every { rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, KEY, capture(requestSlot)) } answers { nothing }

        testInstance.submissionFinalized("S-BSST0", 1)

        val request = requestSlot.captured
        assertThat(request.version).isEqualTo(1)
        assertThat(request.accNo).isEqualTo("S-BSST0")
        verify(exactly = 1) { rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, KEY, request) }
    }

    private companion object {
        const val KEY = "key"
    }
}
