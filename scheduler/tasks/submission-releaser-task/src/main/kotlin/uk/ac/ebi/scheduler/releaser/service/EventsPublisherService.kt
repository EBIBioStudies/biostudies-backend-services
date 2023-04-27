package uk.ac.ebi.scheduler.releaser.service

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.events.SUBMISSIONS_PUBLISHED_ROUTING_KEY
import ebi.ac.uk.extended.events.SubmissionMessage
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.EventsProperties

class EventsPublisherService(
    private val rabbitTemplate: RabbitTemplate,
    private val eventsProperties: EventsProperties,
) {
    fun subToBePublished(accNo: String, owner: String) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE,
            SUBMISSIONS_PUBLISHED_ROUTING_KEY,
            SubmissionMessage.createNew(accNo, owner, eventsProperties.instanceBaseUrl)
        )
}
