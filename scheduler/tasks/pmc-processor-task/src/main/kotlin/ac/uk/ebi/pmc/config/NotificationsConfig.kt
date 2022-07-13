package ac.uk.ebi.pmc.config

import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
internal class NotificationsConfig {
    @Bean
    fun restTemplate(): RestTemplate = JacksonFactory.jsonRestTemplate()

    @Bean
    fun notificationsSender(appProperties: PmcImporterProperties): NotificationsSender =
        NotificationsSender(restTemplate(), appProperties.notificationsUrl)
}
