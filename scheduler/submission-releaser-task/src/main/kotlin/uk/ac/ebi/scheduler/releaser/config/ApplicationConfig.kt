package uk.ac.ebi.scheduler.releaser.config

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.events.config.EventsProperties
import uk.ac.ebi.events.config.EventsPublisherConfig
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.scheduler.releaser.SubmissionReleaserExecutor
import uk.ac.ebi.scheduler.releaser.service.SubmissionReleaserService

@Configuration
class ApplicationConfig {
    @Bean
    fun submissionReleaserService(
        bioWebClient: BioWebClient,
        appProperties: ApplicationProperties,
        eventsPublisherService: EventsPublisherService
    ): SubmissionReleaserService =
        SubmissionReleaserService(bioWebClient, appProperties.notificationTimes, eventsPublisherService)

    @Bean
    fun submissionReleaserExecutor(
        applicationProperties: ApplicationProperties,
        submissionReleaserService: SubmissionReleaserService
    ): SubmissionReleaserExecutor = SubmissionReleaserExecutor(applicationProperties, submissionReleaserService)

    @Bean
    fun bioWebClient(applicationProperties: ApplicationProperties): BioWebClient =
        SecurityWebClient
            .create(applicationProperties.bioStudies.url)
            .getAuthenticatedClient(applicationProperties.bioStudies.user, applicationProperties.bioStudies.password)

    @Bean
    fun eventsProperties(
        applicationProperties: ApplicationProperties
    ): EventsProperties = EventsProperties(instanceBaseUrl = applicationProperties.bioStudies.url)

    @Bean
    fun eventsPublisherConfig(
        eventsProperties: EventsProperties,
        connectionFactory: ConnectionFactory
    ): EventsPublisherConfig = EventsPublisherConfig(eventsProperties, connectionFactory)

    @Bean
    fun eventsPublisherService(
        eventsPublisherConfig: EventsPublisherConfig
    ): EventsPublisherService = eventsPublisherConfig.eventsPublisherService()
}
