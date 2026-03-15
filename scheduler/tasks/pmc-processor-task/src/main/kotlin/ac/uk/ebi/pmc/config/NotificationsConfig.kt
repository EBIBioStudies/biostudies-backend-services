package ac.uk.ebi.pmc.config

import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.util.function.Consumer

@Configuration
internal class NotificationsConfig {
    @Bean
    @Qualifier(BEAN_QUALIFIER)
    fun pmcWebClient(): WebClient {
        val strategies =
            ExchangeStrategies
                .builder()
                .codecs(
                    Consumer { configurer ->
                        configurer
                            .defaultCodecs()
                            .maxInMemorySize(TEN_MGB)
                    },
                ).build()
        return WebClient.builder().exchangeStrategies(strategies).build()
    }

    @Bean
    fun notificationsSender(
        @Qualifier(BEAN_QUALIFIER) pmcWebClient: WebClient,
        appProperties: PmcImporterProperties,
    ): NotificationsSender = NotificationsSender(pmcWebClient, appProperties.notificationsUrl)

    companion object {
        const val TEN_MGB = 10 * 1024 * 1024
        const val BEAN_QUALIFIER = "pmcWebClient"
    }
}
