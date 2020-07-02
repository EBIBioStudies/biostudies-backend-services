package ac.uk.ebi.biostd.handlers.config

import ac.uk.ebi.biostd.handlers.listeners.LogSubmissionListener
import ac.uk.ebi.biostd.handlers.listeners.SubmissionNotificationsListener
import ebi.ac.uk.notifications.integration.NotificationConfig
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository
import ebi.ac.uk.notifications.service.RtNotificationService
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
class Listeners {
    @Bean
    fun logListener(): LogSubmissionListener = LogSubmissionListener()

    @Bean
    fun notificationsListener(
        rtNotificationService: RtNotificationService
    ): SubmissionNotificationsListener = SubmissionNotificationsListener(rtNotificationService)
}

@Configuration
class Services {
    @Bean
    fun rtNotificationService(notificationConfig: NotificationConfig) = notificationConfig.rtNotificationService()
}

@Configuration
class ModuleConfig{
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
