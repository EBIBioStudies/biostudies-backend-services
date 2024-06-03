package ac.uk.ebi.biostd.handlers.exception

import ac.uk.ebi.biostd.handlers.common.HANDLERS_SUBSYSTEM
import ac.uk.ebi.biostd.handlers.common.SYSTEM_NAME
import ac.uk.ebi.biostd.handlers.exception.CustomErrorHandler.Companion.ERROR_MESSAGE
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class CustomErrorHandlerTest(
    @MockK private val notificationsSender: NotificationsSender,
) {
    private val testInstance = CustomErrorHandler(notificationsSender)

    @Test
    fun `handle error`() {
        val alertSlot = slot<Alert>()
        val expectedAlert = Alert(SYSTEM_NAME, HANDLERS_SUBSYSTEM, "$ERROR_MESSAGE: the error message")
        val expection = RuntimeException("the error message")

        coEvery { notificationsSender.send(capture(alertSlot)) } answers { nothing }

        assertFailsWith<AmqpRejectAndDontRequeueException> { testInstance.handleError(expection) }

        val alert = alertSlot.captured
        assertThat(alert).isEqualToComparingFieldByField(expectedAlert)
        coVerify(exactly = 1) { notificationsSender.send(alert) }
    }
}
