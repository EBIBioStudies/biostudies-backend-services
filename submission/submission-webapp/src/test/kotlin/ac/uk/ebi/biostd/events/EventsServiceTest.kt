package ac.uk.ebi.biostd.events

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ebi.ac.uk.extended.events.SubmissionSubmitted
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate

@ExtendWith(MockKExtension::class)
class EventsServiceTest(
    @MockK private val rabbitTemplate: RabbitTemplate,
    @MockK private val properties: ApplicationProperties
) {
    private val testInstance = EventsService(rabbitTemplate, properties)

    @Test
    fun submissionSubmitted(
        @MockK user: SecurityUser,
        @MockK submission: ExtSubmission
    ) {
        val notificationSlot = slot<SubmissionSubmitted>()

        every { user.id } returns 5
        every { submission.accNo } returns "S-BSST0"
        every { properties.instanceBaseUrl } returns "http://biostudies:8788"
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, capture(notificationSlot))
        } answers { nothing }

        testInstance.submissionSubmitted(submission, user)

        val notification = notificationSlot.captured
        assertThat(notification.accNo).isEqualTo("S-BSST0")
        assertThat(notification.uiUrl).isEqualTo("http://biostudies:8788")
        assertThat(notification.pagetabUrl).isEqualTo("http://biostudies:8788/submissions/S-BSST0.json")
        assertThat(notification.extUserUrl).isEqualTo("http://biostudies:8788/security/users/extended/5")
        assertThat(notification.extTabUrl).isEqualTo("http://biostudies:8788/submissions/extended/S-BSST0")

        verify { rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, notification) }
    }
}
