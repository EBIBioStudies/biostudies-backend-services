package ebi.ac.uk.notifications.integration

import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.notifications.service.SimpleEmailService
import ebi.ac.uk.notifications.service.SimpleSubscriptionService
import org.springframework.mail.javamail.JavaMailSenderImpl

class NotificationConfig(private val emailProps: NotificationProperties) {

    fun subscriptionService(): SubscriptionService = subscriptionService

    private val subscriptionService: SubscriptionService by lazy { SimpleSubscriptionService(emailService) }
    private val emailService by lazy { SimpleEmailService(mailSender) }
    private val mailSender by lazy { JavaMailSenderImpl().apply { javaMailProperties = emailProps.asProperties() } }
}
