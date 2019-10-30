package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.persistence.integration.PersistenceContextImpl
import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.JdbcLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Configuration
@EnableJpaRepositories(basePackageClasses = [SubmissionDataRepository::class])
class PersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val tagsDataRepository: AccessTagDataRepository,
    private val tagsRefRepository: TagDataRepository,
    private val userRepository: UserDataRepository,
    private val template: NamedParameterJdbcTemplate,
    private val userDataRepository: UserDataDataRepository
    ) {
    @Bean
    fun submissionRepository() = SubmissionRepository(submissionDataRepository, submissionDbMapper())

    @Bean
    fun submissionDbMapper() = SubmissionDbMapper()

    @Bean
    fun submissionMapper() = SubmissionMapper(tagsDataRepository, tagsRefRepository, userRepository)

    @Bean
    @ConditionalOnMissingBean(LockExecutor::class)
    fun lockExecutor(): LockExecutor = JdbcLockExecutor(template)

    @Bean
    fun persistenceContext(lockExecutor: LockExecutor) =
        PersistenceContextImpl(
            submissionDataRepository,
            sequenceRepository,
            tagsDataRepository,
            lockExecutor,
            submissionDbMapper(),
            submissionMapper(),
            userDataRepository
            )
}
