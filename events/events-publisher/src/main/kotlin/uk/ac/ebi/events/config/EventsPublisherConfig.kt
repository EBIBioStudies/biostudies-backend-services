package uk.ac.ebi.events.config

import ac.uk.ebi.biostd.common.properties.SubmissionNotificationsProperties
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import uk.ac.ebi.events.service.EventsPublisherService

class EventsPublisherConfig(
    private val eventsProperties: EventsProperties,
    private val connectionFactory: ConnectionFactory,
    private val properties: SubmissionNotificationsProperties,
) {
    fun eventsPublisherService(): EventsPublisherService = eventsPublisherService

    private val eventsPublisherService by lazy {
        EventsPublisherService(
            rabbitTemplate,
            eventsProperties,
            properties,
        )
    }

    private val rabbitTemplate by lazy {
        RabbitTemplate(connectionFactory).apply {
            messageConverter = Jackson2JsonMessageConverter()
        }
    }
}
