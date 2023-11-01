package ac.uk.ebi.biostd.common.config.internal

import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.RetryHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {

    @Bean
    fun startApplicationHandler(
        extSubmissionService: ExtSubmissionService,
        requestService: SubmissionRequestPersistenceService,
    ) = RetryHandler(extSubmissionService, requestService)
}
