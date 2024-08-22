package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.itest.common.TestConfig.Companion.SUBMISSION_SUBMITTED_QUEUE
import ebi.ac.uk.extended.events.SubmissionMessage
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.core.ParameterizedTypeReference

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
