package uk.ac.ebi.biostd.itest.common

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.core.ParameterizedTypeReference
import uk.ac.ebi.biostd.extended.events.SubmissionMessage
import uk.ac.ebi.biostd.itest.common.TestConfig.Companion.SUBMISSION_SUBMITTED_QUEUE

class TestMessageService(
    private val rabbitTemplate: RabbitTemplate,
) {
    private val messages = mutableListOf<SubmissionMessage>()

    fun findSubmittedMessages(accNo: String): SubmissionMessage? {
        loadAllMessages()
        return messages.find { it.accNo == accNo }
    }

    private fun loadAllMessages() {
        var message = popMessage()
        while (message != null) {
            messages.add(message)
            message = popMessage()
        }
    }

    private fun popMessage(): SubmissionMessage? {
        val message =
            rabbitTemplate.receiveAndConvert(
                SUBMISSION_SUBMITTED_QUEUE,
                object : ParameterizedTypeReference<SubmissionMessage>() {},
            )
        return message
    }
}
