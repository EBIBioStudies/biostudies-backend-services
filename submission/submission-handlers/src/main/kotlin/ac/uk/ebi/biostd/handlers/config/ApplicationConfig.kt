package ac.uk.ebi.biostd.handlers.config

import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ac.uk.ebi.biostd.handlers.listeners.LogSubmissionListener
import ac.uk.ebi.biostd.handlers.listeners.SecurityNotificationListener
import ac.uk.ebi.biostd.handlers.listeners.SubmissionNotificationsListener
import ebi.ac.uk.notifications.integration.NotificationConfig
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository
import ebi.ac.uk.notifications.service.RtNotificationService
import ebi.ac.uk.notifications.service.SecurityNotificationService
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class Listeners {
    @Bean
    fun logListener(): LogSubmissionListener = LogSubmissionListener()

    @Bean
    fun notificationsListener(
        webConsumer: BioStudiesWebConsumer,
        applicationProperties: ApplicationProperties,
        rtNotificationService: RtNotificationService
    ): SubmissionNotificationsListener =
        SubmissionNotificationsListener(webConsumer, rtNotificationService, applicationProperties.notifications)

    @Bean
    fun securityNotificationsListener(
        securityNotificationService: SecurityNotificationService
    ): SecurityNotificationListener = SecurityNotificationListener(securityNotificationService)
}

@Configuration
class Services {
    @Bean
    fun bioStudiesWebConsumer(
        restTemplate: RestTemplate,
        extSerializationService: ExtSerializationService
    ): BioStudiesWebConsumer = BioStudiesWebConsumer(restTemplate, extSerializationService)

    @Bean
    fun extSerializationService(): ExtSerializationService = ExtSerializationService()

    @Bean
    fun rtNotificationService(notificationConfig: NotificationConfig) = notificationConfig.rtNotificationService()

    @Bean
    fun securityNotificationService(
        notificationConfig: NotificationConfig
    ): SecurityNotificationService = notificationConfig.securityNotificationService()
}

@Configuration
class ModuleConfig {
    @Bean
    fun notificationConfig(
        resourceLoader: ResourceLoader,
        rtRepository: SubmissionRtRepository,
        applicationProperties: ApplicationProperties
    ): NotificationConfig = NotificationConfig(resourceLoader, applicationProperties.notifications, rtRepository)
}

@Configuration
@EnableJpaRepositories(basePackageClasses = [SubmissionRtRepository::class])
@EntityScan(basePackages = ["ebi.ac.uk.notifications.persistence.model"])
class PersistenceConfig
