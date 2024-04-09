package uk.ac.ebi.scheduler.releaser.service

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.events.SUBMISSIONS_PUBLISHED_ROUTING_KEY
import ebi.ac.uk.extended.events.SubmissionMessage
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.EventsProperties

@ExtendWith(MockKExtension::class)
class EventsPublisherServiceTest(
    @MockK private val rabbitTemplate: RabbitTemplate,
    @MockK private val eventsProperties: EventsProperties,
) {
    private val testInstance: EventsPublisherService = EventsPublisherService(rabbitTemplate, eventsProperties)

    @Test
    fun submissionReleased() {
        val notificationSlot = slot<SubmissionMessage>()

        every { eventsProperties.instanceBaseUrl } returns "http://biostudies:8788"
        every {
            rabbitTemplate.convertAndSend(
                BIOSTUDIES_EXCHANGE, SUBMISSIONS_PUBLISHED_ROUTING_KEY, capture(notificationSlot),
            )
        } answers { nothing }

        testInstance.subToBePublished("S-BSST0", "test@ebi.ac.uk")

        val notification = notificationSlot.captured
        Assertions.assertThat(notification.accNo).isEqualTo("S-BSST0")
        Assertions.assertThat(notification.pagetabUrl).isEqualTo("http://biostudies:8788/submissions/S-BSST0.json")
        Assertions.assertThat(notification.extTabUrl).isEqualTo("http://biostudies:8788/submissions/extended/S-BSST0")
        Assertions.assertThat(notification.extUserUrl)
            .isEqualTo("http://biostudies:8788/security/users/extended/test@ebi.ac.uk")

        verify(exactly = 1) {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_PUBLISHED_ROUTING_KEY, notification)
        }
    }
}
