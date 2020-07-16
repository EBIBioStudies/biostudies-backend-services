package ebi.ac.uk.notifications.integration

import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository
import ebi.ac.uk.notifications.persistence.service.NotificationPersistenceService
import ebi.ac.uk.notifications.service.RtNotificationService
import ebi.ac.uk.notifications.service.SimpleEmailService
import ebi.ac.uk.notifications.service.SimpleSubscriptionService
import org.springframework.core.io.ResourceLoader
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.web.client.RestTemplate

class NotificationConfig(
    private val resourceLoader: ResourceLoader,
    private val notificationProperties: NotificationProperties,
    private var submissionRtRepository: SubmissionRtRepository
) {
    fun subscriptionService(): SubscriptionService = subscriptionService

    fun rtNotificationService(): RtNotificationService = rtNotificationService

    private val restTemplate by lazy { RestTemplate() }

    private val emailService by lazy { SimpleEmailService(mailSender) }

    private val subscriptionService: SubscriptionService by lazy { SimpleSubscriptionService(emailService) }

    private val mailSender by lazy { JavaMailSenderImpl().apply {
        javaMailProperties = notificationProperties.asProperties() }
    }

    private val notificationPersistenceService: NotificationPersistenceService by lazy {
        NotificationPersistenceService(submissionRtRepository)
    }

    private val rtNotificationService: RtNotificationService by lazy {
        RtNotificationService(
            RtClient(notificationProperties.rt, restTemplate),
            resourceLoader,
            notificationPersistenceService)
    }
}
