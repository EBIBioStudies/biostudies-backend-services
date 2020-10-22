package uk.ac.ebi.scheduler.common.config

import uk.ac.ebi.scheduler.common.properties.AppProperties
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
    fun notificationsSender(appProperties: AppProperties): NotificationsSender =
        NotificationsSender(restTemplate(), appProperties.notificationsUrl)
}
