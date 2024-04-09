package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.events.config.EventsProperties
import uk.ac.ebi.events.config.EventsPublisherConfig
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class JmsPublishingConfig(
    private val applicationProperties: ApplicationProperties,
) {
    @Bean
    fun eventsProperties(): EventsProperties = EventsProperties(instanceBaseUrl = applicationProperties.instanceBaseUrl)

    @Bean
    fun eventsPublisherConfig(
        eventsProperties: EventsProperties,
        connectionFactory: ConnectionFactory,
    ): EventsPublisherConfig = EventsPublisherConfig(eventsProperties, connectionFactory, applicationProperties.notifications)

    @Bean
    fun eventsPublisherService(eventsPublisherConfig: EventsPublisherConfig): EventsPublisherService =
        eventsPublisherConfig.eventsPublisherService()

    @Bean
    fun myRabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = messageConverter()
        return rabbitTemplate
    }

    @Bean
    fun messageConverter(): MessageConverter = Jackson2JsonMessageConverter(ExtSerializationService.mapper)
}
