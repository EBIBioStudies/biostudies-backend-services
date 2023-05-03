package uk.ac.ebi.scheduler.common.config

import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.scheduler.common.properties.AppProperties

@Configuration
@EnableConfigurationProperties
internal class NotificationsConfig {
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    @Bean
    fun schedulerNotificationsSender(
        restTemplate: RestTemplate,
        appProperties: AppProperties
    ): NotificationsSender = NotificationsSender(
        restTemplate,
        appProperties.slack.schedulerNotificationsUrl,
    )

    @Bean
    fun pmcNotificationsSender(
        restTemplate: RestTemplate,
        appProperties: AppProperties
    ): NotificationsSender = NotificationsSender(restTemplate, appProperties.slack.pmcNotificationsUrl)
}
