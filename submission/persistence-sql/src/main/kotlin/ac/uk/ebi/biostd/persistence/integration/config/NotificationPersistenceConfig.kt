package ac.uk.ebi.biostd.persistence.integration.config

import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ac.uk.ebi.biostd.persistence.integration.services.NotificationsSqlDataService
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRtRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(JpaRepositoryConfig::class)
open class NotificationPersistenceConfig {

    @Bean
    internal open fun notificationsDataService(
        submissionRtRepository: SubmissionRtRepository
    ): NotificationsDataService = NotificationsSqlDataService(submissionRtRepository)
}
