package uk.ac.ebi.scheduler.common.config

import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.scheduler.common.properties.AppProperties

@Configuration
@EnableConfigurationProperties
internal class NotificationsConfig {
    @Bean
    fun webClient(): WebClient = WebClient.builder().build()

    @Bean
    @Qualifier(SCHEDULER_NOTIFICATIONS_SENDER)
    fun schedulerNotificationsSender(
        client: WebClient,
        appProperties: AppProperties,
    ): NotificationsSender =
        NotificationsSender(
            client,
            appProperties.slack.schedulerNotificationsUrl,
        )

    @Bean
    @Qualifier(PMC_NOTIFICATIONS_SENDER)
    fun pmcNotificationsSender(
        client: WebClient,
        appProperties: AppProperties,
    ): NotificationsSender = NotificationsSender(client, appProperties.slack.pmcNotificationsUrl)

    companion object {
        const val PMC_NOTIFICATIONS_SENDER = "pmcNotificationsSender"
        const val SCHEDULER_NOTIFICATIONS_SENDER = "schedulerNotificationsSender"
    }
}
