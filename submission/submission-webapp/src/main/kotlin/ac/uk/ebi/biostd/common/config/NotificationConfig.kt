package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.notifications.NotificationsSubscriber
import ebi.ac.uk.notifications.integration.NotificationConfig
import ebi.ac.uk.notifications.integration.NotificationProperties
import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserPreRegister
import io.reactivex.Observable
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
internal class NotificationConfig {

    @Bean
    fun emailConfig(): NotificationConfig {
        return NotificationConfig(NotificationProperties("smtp.ebi.ac.uk"))
    }

    @Bean
    fun subscriptionService(emailConfig: NotificationConfig): SubscriptionService = emailConfig.subscriptionService()

    @Bean
    fun notificationService(
        subscriptionService: SubscriptionService,
        resourceLoader: ResourceLoader,
        userPreRegister: Observable<UserPreRegister>,
        passwordReset: Observable<PasswordReset>
    ): NotificationsSubscriber = NotificationsSubscriber(subscriptionService, resourceLoader, userPreRegister, passwordReset)
}
