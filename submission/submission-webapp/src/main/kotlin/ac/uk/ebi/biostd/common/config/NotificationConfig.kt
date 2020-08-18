package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ebi.ac.uk.notifications.integration.NotificationConfig
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
@ConditionalOnProperty("app.notifications.smtp")
internal class NotificationConfig(
    private val properties: ApplicationProperties,
    private val submissionRtRepository: SubmissionRtRepository
) {
    @Bean
    fun emailConfig(resourceLoader: ResourceLoader): NotificationConfig =
        NotificationConfig(resourceLoader, properties.notifications, submissionRtRepository)
}
