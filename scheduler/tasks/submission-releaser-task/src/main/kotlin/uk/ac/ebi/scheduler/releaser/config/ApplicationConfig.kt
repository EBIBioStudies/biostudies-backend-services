package uk.ac.ebi.scheduler.releaser.config

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.persistence.doc.MongoDbReactiveConfig
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionReleaserRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionRequestRepository
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.events.config.EventsProperties
import uk.ac.ebi.scheduler.releaser.SubmissionReleaserExecutor
import uk.ac.ebi.scheduler.releaser.service.EventsPublisherService
import uk.ac.ebi.scheduler.releaser.service.SubmissionReleaserService

@Configuration
@Import(MongoDbReactiveConfig::class)
class ApplicationConfig(
    private val appProperties: ApplicationProperties,
    private val releaserRepository: SubmissionReleaserRepository,
) {
    @Bean
    fun submissionReleaserService(
        bioWebClient: BioWebClient,
        eventsPublisherService: EventsPublisherService,
        requestRepository: SubmissionRequestRepository,
    ): SubmissionReleaserService =
        SubmissionReleaserService(
            bioWebClient,
            appProperties.notificationTimes,
            releaserRepository,
            requestRepository,
            eventsPublisherService,
        )

    @Bean
    fun submissionReleaserExecutor(
        applicationProperties: ApplicationProperties,
        submissionReleaserService: SubmissionReleaserService,
    ): SubmissionReleaserExecutor = SubmissionReleaserExecutor(applicationProperties, submissionReleaserService)

    @Bean
    fun bioWebClient(applicationProperties: ApplicationProperties): BioWebClient =
        SecurityWebClient
            .create(applicationProperties.bioStudies.url)
            .getAuthenticatedClient(applicationProperties.bioStudies.user, applicationProperties.bioStudies.password)

    @Bean
    fun eventsProperties(applicationProperties: ApplicationProperties): EventsProperties =
        EventsProperties(instanceBaseUrl = applicationProperties.bioStudies.url)

    @Bean
    fun eventsPublisherService(
        rabbitTemplate: RabbitTemplate,
        eventsProperties: EventsProperties,
    ): EventsPublisherService {
        rabbitTemplate.messageConverter = Jackson2JsonMessageConverter()
        return EventsPublisherService(rabbitTemplate, eventsProperties)
    }
}
