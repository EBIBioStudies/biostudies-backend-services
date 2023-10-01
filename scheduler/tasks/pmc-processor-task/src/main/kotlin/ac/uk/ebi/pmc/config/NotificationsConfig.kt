package ac.uk.ebi.pmc.config

import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
internal class NotificationsConfig {
    @Bean
    fun webClient(): WebClient = WebClient.builder().build()

    @Bean
    fun notificationsSender(
        client: WebClient,
        appProperties: PmcImporterProperties,
    ): NotificationsSender = NotificationsSender(client, appProperties.notificationsUrl)
}
