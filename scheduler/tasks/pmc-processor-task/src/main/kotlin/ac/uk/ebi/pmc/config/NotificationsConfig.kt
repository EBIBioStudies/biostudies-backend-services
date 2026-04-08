package ac.uk.ebi.pmc.config

import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
internal class NotificationsConfig {
    @Bean
    @Qualifier(BEAN_QUALIFIER)
    fun pmcWebClient(): WebClient = WebClient.builder().build()

    @Bean
    fun notificationsSender(
        @Qualifier(BEAN_QUALIFIER) pmcWebClient: WebClient,
        appProperties: PmcImporterProperties,
    ): NotificationsSender = NotificationsSender(pmcWebClient, appProperties.notificationsUrl)

    companion object {
        const val BEAN_QUALIFIER = "pmcWebClient"
    }
}
