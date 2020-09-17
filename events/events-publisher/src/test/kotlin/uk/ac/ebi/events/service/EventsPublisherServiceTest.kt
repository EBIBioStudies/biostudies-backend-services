package uk.ac.ebi.events.service

import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.extended.model.ExtSubmission
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
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.EventsProperties
import uk.ac.ebi.events.config.SECURITY_NOTIFICATIONS_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_RELEASE_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_ROUTING_KEY
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class EventsPublisherServiceTest(
    @MockK private val rabbitTemplate: RabbitTemplate,
    @MockK private val eventsProperties: EventsProperties
) {
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 1, 2, 3, 4, ZoneOffset.UTC)
    private val testInstance = EventsPublisherService(rabbitTemplate, eventsProperties)

    @BeforeEach
    fun beforeEach() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns mockNow
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun securityNotification(@MockK notification: SecurityNotification) {
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SECURITY_NOTIFICATIONS_ROUTING_KEY, notification)
        } answers { nothing }

        testInstance.securityNotification(notification)

        verify(exactly = 1) {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SECURITY_NOTIFICATIONS_ROUTING_KEY, notification)
        }
    }

    @Test
    fun submissionSubmitted(@MockK submission: ExtSubmission) {
        val notificationSlot = slot<SubmissionMessage>()

        every { submission.accNo } returns "S-BSST0"
        every { submission.submitter } returns "test@ebi.ac.uk"
        every { eventsProperties.instanceBaseUrl } returns "http://biostudies:8788"
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, capture(notificationSlot))
        } answers { nothing }

        testInstance.submissionSubmitted(submission)

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
    fun submissionReleased(@MockK submission: ExtSubmission) {
        val notificationSlot = slot<SubmissionMessage>()

        every { submission.accNo } returns "S-BSST0"
        every { submission.submitter } returns "test@ebi.ac.uk"
        every { eventsProperties.instanceBaseUrl } returns "http://biostudies:8788"
        every {
            rabbitTemplate.convertAndSend(
                BIOSTUDIES_EXCHANGE, SUBMISSIONS_RELEASE_ROUTING_KEY, capture(notificationSlot))
        } answers { nothing }

        testInstance.submissionReleased(submission)

        val notification = notificationSlot.captured
        assertThat(notification.accNo).isEqualTo("S-BSST0")
        assertThat(notification.pagetabUrl).isEqualTo("http://biostudies:8788/submissions/S-BSST0.json")
        assertThat(notification.extTabUrl).isEqualTo("http://biostudies:8788/submissions/extended/S-BSST0")
        assertThat(notification.extUserUrl).isEqualTo("http://biostudies:8788/security/users/extended/test@ebi.ac.uk")

        verify(exactly = 1) {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_RELEASE_ROUTING_KEY, notification)
        }
    }
}
