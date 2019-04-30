package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.persistence.integration.PersistenceContextImpl
import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsRefRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackageClasses = [SubmissionDataRepository::class])
class PersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val tagsDataRepository: TagsDataRepository,
    private val tagsRefRepository: TagsRefRepository
) {
    @Bean
    fun submissionRepository() = SubmissionRepository(submissionDataRepository, submissionDbMapper())

    @Bean
    fun submissionDbMapper() = SubmissionDbMapper()

    @Bean
    fun submissionMapper() = SubmissionMapper(tagsDataRepository, tagsRefRepository)

    @Bean
    fun persistenceContext() = PersistenceContextImpl(
        submissionDataRepository, sequenceRepository, submissionDbMapper(), submissionMapper())
}
