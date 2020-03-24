package ebi.ac.uk.notifications.integration

import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository
import ebi.ac.uk.notifications.persistence.service.NotificationPersistenceService
import ebi.ac.uk.notifications.service.RtSubscriptionService
import ebi.ac.uk.notifications.service.SimpleEmailService
import ebi.ac.uk.notifications.service.SimpleSubscriptionService
import org.springframework.mail.javamail.JavaMailSenderImpl

class NotificationConfig(
    private val notificationProperties: NotificationProperties,
    private var submissionRtRepository: SubmissionRtRepository
) {
    fun subscriptionService(): SubscriptionService = subscriptionService

    fun rtSubscriptionService(): SubscriptionService = rtSubscriptionService

    private val emailService by lazy { SimpleEmailService(mailSender) }

    private val subscriptionService: SubscriptionService by lazy { SimpleSubscriptionService(emailService) }

    private val mailSender by lazy { JavaMailSenderImpl().apply {
        javaMailProperties = notificationProperties.asProperties() }
    }

    private val notificationPersistenceService: NotificationPersistenceService by lazy {
        NotificationPersistenceService(submissionRtRepository)
    }

    private val rtSubscriptionService: SubscriptionService by lazy {
        RtSubscriptionService(RtClient(notificationProperties.rt), notificationPersistenceService)
    }
}
