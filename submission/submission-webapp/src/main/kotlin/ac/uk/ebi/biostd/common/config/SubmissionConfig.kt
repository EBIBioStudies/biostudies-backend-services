package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.submission.service.SubmissionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(PersistenceConfig::class)
class SubmissionConfig {

    @Bean
    fun submissionService(): SubmissionService = TODO()
}
