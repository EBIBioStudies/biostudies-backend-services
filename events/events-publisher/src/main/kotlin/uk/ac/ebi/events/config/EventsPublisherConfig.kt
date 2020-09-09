package uk.ac.ebi.events.config

import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import uk.ac.ebi.events.service.EventsPublisherService

const val BIOSTUDIES_EXCHANGE = "biostudies-exchange"
const val SUBMISSIONS_ROUTING_KEY = "bio.submission.published"
const val SUBMISSIONS_RELEASE_ROUTING_KEY = "bio.submission.published.notification"
const val SUBMISSIONS_REQUEST_ROUTING_KEY = "bio.submission.requested"
const val SECURITY_NOTIFICATIONS_ROUTING_KEY = "bio.security.notification"

class EventsPublisherConfig(
    private val eventsProperties: EventsProperties,
    private val connectionFactory: ConnectionFactory
) {
    fun eventsPublisherService(): EventsPublisherService = eventsPublisherService

    private val eventsPublisherService by lazy { EventsPublisherService(rabbitTemplate, eventsProperties) }

    private val rabbitTemplate by lazy {
        RabbitTemplate(connectionFactory).apply {
            messageConverter = Jackson2JsonMessageConverter()
        }
    }
}
