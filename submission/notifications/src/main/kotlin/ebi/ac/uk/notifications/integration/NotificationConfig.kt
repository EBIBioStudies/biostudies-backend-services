package ebi.ac.uk.notifications.integration

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.service.RtNotificationService
import ebi.ac.uk.notifications.service.RtTicketService
import ebi.ac.uk.notifications.service.SecurityNotificationService
import ebi.ac.uk.notifications.service.SimpleEmailService
import ebi.ac.uk.notifications.util.TemplateLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.web.reactive.function.client.WebClient

class NotificationConfig(
    private val resourceLoader: ResourceLoader,
    private val properties: NotificationProperties,
    private val notificationDataService: NotificationsDataService,
) {
    fun rtNotificationService(): RtNotificationService = rtNotificationService

    fun securityNotificationService(): SecurityNotificationService = securityNotificationService

    private val webClient by lazy { WebClient.builder().build() }

    private val emailService by lazy { SimpleEmailService(mailSender) }

    private val templateLoader by lazy { TemplateLoader(resourceLoader) }

    private val securityNotificationService by lazy {
        SecurityNotificationService(
            templateLoader,
            emailService,
            properties
        )
    }

    private val mailSender by lazy {
        JavaMailSenderImpl().apply {
            javaMailProperties = properties.asProperties()
        }
    }

    private val rtTicketService: RtTicketService by lazy {
        RtTicketService(notificationDataService, properties, RtClient(properties.rt, webClient))
    }

    private val rtNotificationService: RtNotificationService by lazy {
        RtNotificationService(templateLoader, rtTicketService)
    }
}
