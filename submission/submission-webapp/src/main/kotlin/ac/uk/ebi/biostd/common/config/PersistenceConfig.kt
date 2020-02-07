package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.persistence.integration.PersistenceContextImpl
import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.JdbcLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.service.ProjectRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Configuration
@EnableJpaRepositories(
    basePackageClasses = [SubmissionDataRepository::class],
    repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean::class)
class PersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val tagsDataRepository: AccessTagDataRepository,
    private val tagsRefRepository: TagDataRepository,
    private val userRepository: UserDataRepository,
    private val template: NamedParameterJdbcTemplate,
    private val userDataRepository: UserDataDataRepository,
    private val applicationProperties: ApplicationProperties
) {
    @Bean
    fun toDbSubmissionMapper(
        tagsRepository: AccessTagDataRepository,
        tagsRefRepository: TagDataRepository,
        userRepository: UserDataRepository
    ) =
        ToDbSubmissionMapper(tagsRepository, tagsRefRepository, userRepository)

    @Bean
    fun toExtSubmissionMapper() = ToExtSubmissionMapper(applicationProperties.submissionsPath)

    @Bean
    fun submissionRepository() = SubmissionRepository(submissionDataRepository, submissionDbMapper())

    @Bean
    fun projectRepository() = ProjectRepository(submissionDataRepository)

    @Bean
    fun submissionDbMapper() = SubmissionDbMapper()

    @Bean
    fun submissionMapper() = SubmissionMapper(tagsDataRepository, tagsRefRepository, userRepository)

    @Bean
    @ConditionalOnMissingBean(LockExecutor::class)
    fun lockExecutor(): LockExecutor = JdbcLockExecutor(template)

    @Bean
    fun persistenceContext(
        lockExecutor: LockExecutor,
        dbSubmissionMapper: ToDbSubmissionMapper,
        toExtSubmissionMapper: ToExtSubmissionMapper
    ) =
        PersistenceContextImpl(
            submissionDataRepository,
            sequenceRepository,
            tagsDataRepository,
            lockExecutor,
            submissionDbMapper(),
            submissionMapper(),
            userDataRepository,
            dbSubmissionMapper,
            toExtSubmissionMapper
        )
}
