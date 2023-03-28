package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.events.config.EventsProperties
import uk.ac.ebi.events.config.EventsPublisherConfig
import uk.ac.ebi.events.service.EventsPublisherService

@Configuration
class EventsConfig(
    private val applicationProperties: ApplicationProperties,
) {
    @Bean
    fun eventsProperties(): EventsProperties =
        EventsProperties(instanceBaseUrl = applicationProperties.instanceBaseUrl)

    @Bean
    fun eventsPublisherConfig(
        eventsProperties: EventsProperties,
        connectionFactory: ConnectionFactory,
    ): EventsPublisherConfig =
        EventsPublisherConfig(eventsProperties, connectionFactory, applicationProperties.notifications)

    @Bean
    fun eventsPublisherService(
        eventsPublisherConfig: EventsPublisherConfig,
    ): EventsPublisherService = eventsPublisherConfig.eventsPublisherService()
}
