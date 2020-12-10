package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ebi.ac.uk.notifications.integration.NotificationConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
@ConditionalOnProperty("app.notifications.smtp")
internal class NotificationConfig(
    private val properties: ApplicationProperties,
    private val notificationsDataService: NotificationsDataService
) {
    @Bean
    fun emailConfig(resourceLoader: ResourceLoader): NotificationConfig =
        NotificationConfig(resourceLoader, properties.notifications, notificationsDataService)
}
