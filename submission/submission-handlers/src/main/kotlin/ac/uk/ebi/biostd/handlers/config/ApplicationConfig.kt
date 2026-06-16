package ac.uk.ebi.biostd.handlers.config

import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ac.uk.ebi.biostd.handlers.exception.CustomErrorHandler
import ac.uk.ebi.biostd.handlers.listeners.CleanUpNotificationListener
import ac.uk.ebi.biostd.handlers.listeners.LogSubmissionListener
import ac.uk.ebi.biostd.handlers.listeners.SecurityNotificationListener
import ac.uk.ebi.biostd.handlers.listeners.SubmissionNotificationsListener
import ac.uk.ebi.biostd.persistence.common.service.NotificationErrorDataService
import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.NotificationErrorMongoRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.integration.SerializationConfiguration
import ac.uk.ebi.biostd.persistence.doc.service.NotificationErrorMongoDataService
import ac.uk.ebi.biostd.persistence.integration.config.NotificationPersistenceConfig
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.notifications.integration.NotificationConfig
import ebi.ac.uk.notifications.service.CleanUpNotificationService
import ebi.ac.uk.notifications.service.RtNotificationService
import ebi.ac.uk.notifications.service.SecurityNotificationService
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.io.ResourceLoader
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
@Import(NotificationPersistenceConfig::class, MongoDbReposConfig::class)
@EnableConfigurationProperties(ApplicationProperties::class)
class Listeners {
    @Bean
    fun logListener(): LogSubmissionListener = LogSubmissionListener()

    @Bean
    fun customErrorHandler(notificationsSender: NotificationsSender): ConditionalRejectingErrorHandler =
        CustomErrorHandler(notificationsSender)

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        factory: SimpleRabbitListenerContainerFactory,
        errorHandler: ConditionalRejectingErrorHandler,
        configurer: SimpleRabbitListenerContainerFactoryConfigurer,
    ): RabbitTemplate {
        factory.setErrorHandler(errorHandler)
        configurer.configure(factory, connectionFactory)
        return RabbitTemplate(connectionFactory).apply { messageConverter = Jackson2JsonMessageConverter() }
    }

    @Bean
    fun notificationsListener(
        rabbitTemplate: RabbitTemplate,
        webConsumer: BioStudiesWebConsumer,
        notificationsSender: NotificationsSender,
        applicationProperties: ApplicationProperties,
        rtNotificationService: RtNotificationService,
        notificationErrorDataService: NotificationErrorDataService,
    ): SubmissionNotificationsListener =
        SubmissionNotificationsListener(
            rabbitTemplate,
            webConsumer,
            notificationsSender,
            rtNotificationService,
            applicationProperties.notifications,
            notificationErrorDataService,
        )

    @Bean
    fun notificationErrorDataService(notificationErrorMongoRepository: NotificationErrorMongoRepository): NotificationErrorDataService =
        NotificationErrorMongoDataService(notificationErrorMongoRepository)

    @Bean
    fun securityNotificationsListener(
        rabbitTemplate: RabbitTemplate,
        notificationsSender: NotificationsSender,
        securityNotificationService: SecurityNotificationService,
    ): SecurityNotificationListener =
        SecurityNotificationListener(
            rabbitTemplate,
            notificationsSender,
            securityNotificationService,
        )

    @Bean
    fun cleanUpNotificationsListener(
        rabbitTemplate: RabbitTemplate,
        notificationsSender: NotificationsSender,
        cleanUpNotificationService: CleanUpNotificationService,
        notificationErrorService: NotificationErrorDataService,
    ): CleanUpNotificationListener =
        CleanUpNotificationListener(
            rabbitTemplate,
            notificationsSender,
            cleanUpNotificationService,
            notificationErrorService,
        )
}

@Configuration
@Import(SerializationConfiguration::class)
class WebConfig {
    @Bean
    fun jsonMessageConverter(): MessageConverter = Jackson2JsonMessageConverter()

    @Bean
    fun webClient(): WebClient {
        val exchangeStrategies =
            ExchangeStrategies
                .builder()
                .codecs { configurer ->
                    if (configurer is ClientCodecConfigurer) configurer.defaultCodecs().maxInMemorySize(-1)
                }.build()

        return WebClient
            .builder()
            .exchangeStrategies(exchangeStrategies)
            .build()
    }

    @Bean
    fun bioStudiesWebConsumer(
        client: WebClient,
        extSerializationService: ExtSerializationService,
    ): BioStudiesWebConsumer = BioStudiesWebConsumer(client, extSerializationService)

    @Bean
    fun notificationsSender(
        client: WebClient,
        applicationProperties: ApplicationProperties,
    ): NotificationsSender = NotificationsSender(client, applicationProperties.notifications.slackUrl)
}

@Configuration
class Services {
    @Bean
    fun rtNotificationService(notificationConfig: NotificationConfig) = notificationConfig.rtNotificationService()

    @Bean
    fun securityNotificationService(notificationConfig: NotificationConfig): SecurityNotificationService =
        notificationConfig.securityNotificationService()

    @Bean
    fun cleanUpNotificationService(notificationConfig: NotificationConfig): CleanUpNotificationService =
        notificationConfig.cleanUpNotificationService()
}

@Configuration
class ModuleConfig {
    @Bean
    fun notificationConfig(
        resourceLoader: ResourceLoader,
        notificationsDataService: NotificationsDataService,
        applicationProperties: ApplicationProperties,
    ): NotificationConfig =
        NotificationConfig(
            resourceLoader,
            applicationProperties.notifications,
            notificationsDataService,
        )
}

@Configuration
class PersistenceConfig
