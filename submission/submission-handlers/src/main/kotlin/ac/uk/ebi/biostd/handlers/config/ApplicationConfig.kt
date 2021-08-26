package ac.uk.ebi.biostd.handlers.config

import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ac.uk.ebi.biostd.handlers.listeners.LogSubmissionListener
import ac.uk.ebi.biostd.handlers.listeners.SecurityNotificationListener
import ac.uk.ebi.biostd.handlers.listeners.SubmissionNotificationsListener
import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ac.uk.ebi.biostd.persistence.integration.config.NotificationPersistenceConfig
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.notifications.integration.NotificationConfig
import ebi.ac.uk.notifications.service.RtNotificationService
import ebi.ac.uk.notifications.service.SecurityNotificationService
import ebi.ac.uk.util.collections.replace
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.io.ResourceLoader
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.nio.charset.StandardCharsets.UTF_8

@Configuration
@Import(NotificationPersistenceConfig::class)
class Listeners {
    @Bean
    fun logListener(): LogSubmissionListener = LogSubmissionListener()

    @Bean
    fun notificationsListener(
        webConsumer: BioStudiesWebConsumer,
        notificationsSender: NotificationsSender,
        applicationProperties: ApplicationProperties,
        rtNotificationService: RtNotificationService
    ): SubmissionNotificationsListener = SubmissionNotificationsListener(
        webConsumer,
        notificationsSender,
        rtNotificationService,
        applicationProperties.notifications
    )

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
    fun notificationsSender(
        restTemplate: RestTemplate,
        applicationProperties: ApplicationProperties
    ): NotificationsSender =
        NotificationsSender(restTemplate, applicationProperties.notifications.slackUrl)

    @Bean
    fun extSerializationService(): ExtSerializationService = ExtSerializationConfig.extSerializationService()

    @Bean
    fun rtNotificationService(notificationConfig: NotificationConfig) = notificationConfig.rtNotificationService()

    @Bean
    fun securityNotificationService(
        notificationConfig: NotificationConfig
    ): SecurityNotificationService = notificationConfig.securityNotificationService()

    private fun getConverters(converters: List<HttpMessageConverter<*>>) =
        converters.replace({ it is StringHttpMessageConverter }, StringHttpMessageConverter(UTF_8))
}

@Configuration
class ModuleConfig {
    @Bean
    fun notificationConfig(
        resourceLoader: ResourceLoader,
        notificationsDataService: NotificationsDataService,
        applicationProperties: ApplicationProperties
    ): NotificationConfig = NotificationConfig(
        resourceLoader,
        applicationProperties.notifications,
        notificationsDataService
    )
}

@Configuration
class PersistenceConfig
