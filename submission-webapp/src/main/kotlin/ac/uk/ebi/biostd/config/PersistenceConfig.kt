package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.persistence.integration.PersistenceContextImpl
import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackageClasses = [SubmissionDataRepository::class])
class PersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val tagsDataRepository: TagsDataRepository

) {

    @Bean
    fun submissionRepository(): SubmissionRepository {
        return SubmissionRepository(submissionDataRepository, submissionDbMapper())
    }

    @Bean
    fun submissionDbMapper(): SubmissionDbMapper {
        return SubmissionDbMapper()
    }

    @Bean
    fun submissionMapper(): SubmissionMapper {
        return SubmissionMapper(tagsDataRepository)
    }

    @Bean
    fun persistenceContext(): PersistenceContext {
        return PersistenceContextImpl(submissionDataRepository, sequenceRepository, submissionDbMapper(), submissionMapper())
    }
}
