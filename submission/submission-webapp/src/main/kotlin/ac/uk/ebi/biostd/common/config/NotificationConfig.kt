package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.notifications.NotificationsSubscriber
import ebi.ac.uk.notifications.integration.NotificationConfig
import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserPreRegister
import io.reactivex.Observable
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
@ConditionalOnProperty("app.notifications.smtp")
internal class NotificationConfig(private val properties: ApplicationProperties) {
    @Bean
    fun emailConfig(): NotificationConfig {
        return NotificationConfig(properties.notifications)
    }

    @Bean
    fun subscriptionService(emailConfig: NotificationConfig): SubscriptionService = emailConfig.subscriptionService()

    @Bean
    fun notificationService(
        subscriptionService: SubscriptionService,
        resourceLoader: ResourceLoader,
        userPreRegister: Observable<UserPreRegister>,
        passwordReset: Observable<PasswordReset>
    ): NotificationsSubscriber =
        NotificationsSubscriber(subscriptionService, resourceLoader, userPreRegister, passwordReset)
}
