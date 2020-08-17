package ebi.ac.uk.notifications.integration

import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository
import ebi.ac.uk.notifications.persistence.service.NotificationPersistenceService
import ebi.ac.uk.notifications.service.RtNotificationService
import ebi.ac.uk.notifications.service.SecurityNotificationService
import ebi.ac.uk.notifications.service.SimpleEmailService
import ebi.ac.uk.notifications.util.TemplateLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.web.client.RestTemplate

class NotificationConfig(
    private val resourceLoader: ResourceLoader,
    private val notificationProperties: NotificationProperties,
    private var submissionRtRepository: SubmissionRtRepository
) {
    fun rtNotificationService(): RtNotificationService = rtNotificationService

    fun securityNotificationService(): SecurityNotificationService = securityNotificationService

    private val restTemplate by lazy { RestTemplate() }

    private val emailService by lazy { SimpleEmailService(mailSender) }

    private val templateLoader by lazy { TemplateLoader(resourceLoader) }

    private val securityNotificationService by lazy { SecurityNotificationService(templateLoader, emailService) }

    private val mailSender by lazy { JavaMailSenderImpl().apply {
        javaMailProperties = notificationProperties.asProperties() }
    }

    private val notificationPersistenceService: NotificationPersistenceService by lazy {
        NotificationPersistenceService(submissionRtRepository)
    }

    private val rtNotificationService: RtNotificationService by lazy {
        RtNotificationService(
            RtClient(notificationProperties.rt, restTemplate),
            templateLoader,
            notificationPersistenceService)
    }
}
