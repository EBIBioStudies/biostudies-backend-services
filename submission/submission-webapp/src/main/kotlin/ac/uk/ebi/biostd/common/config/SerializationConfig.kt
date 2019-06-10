package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.integration.ISerializationService
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.service.SubmissionService
import ebi.ac.uk.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(PersistenceConfig::class)
class SerializationConfig {

    @Bean
    fun submissionService(
        subRepository: SubmissionRepository,
        serializationService: ISerializationService,
        persistenceContext: PersistenceContext,
        submissionSubmitter: SubmissionSubmitter
    ) = SubmissionService(
        subRepository, persistenceContext, serializationService, submissionSubmitter)
}
