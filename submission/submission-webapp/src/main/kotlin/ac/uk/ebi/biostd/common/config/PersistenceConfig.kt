package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.PersistenceContextImpl
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.integration.SubmissionSqlQueryService
import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.JdbcLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.service.FilePersistenceService
import ac.uk.ebi.biostd.persistence.service.ProjectRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean
import ebi.ac.uk.paths.SubmissionFolderResolver
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
    private val tagsDataRepository: AccessTagDataRepo,
    private val template: NamedParameterJdbcTemplate,
    private val userDataRepository: UserDataDataRepository,
    private val applicationProperties: ApplicationProperties,
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    @Bean
    fun toDbSubmissionMapper(
        tagsRepo: AccessTagDataRepo,
        tagsRefRepo: TagDataRepository,
        userRepo: UserDataRepository
    ) = ToDbSubmissionMapper(tagsRepo, tagsRefRepo, userRepo)

    @Bean
    fun toExtSubmissionMapper() = ToExtSubmissionMapper(applicationProperties.submissionsPath)

    @Bean
    fun submissionRepository() = SubmissionRepository(submissionDataRepository, submissionDbMapper())

    @Bean
    fun projectRepository() = ProjectRepository(submissionDataRepository)

    @Bean
    fun submissionDbMapper() = SubmissionDbMapper()

    @Bean
    fun filePersistenceService() = FilePersistenceService(folderResolver, serializationService)

    @Bean
    @ConditionalOnMissingBean(LockExecutor::class)
    fun lockExecutor(): LockExecutor = JdbcLockExecutor(template)

    @Bean
    fun persistenceContext(
        lockExecutor: LockExecutor,
        dbSubmissionMapper: ToDbSubmissionMapper,
        toExtSubmissionMapper: ToExtSubmissionMapper,
        filePersistenceService: FilePersistenceService
    ): PersistenceContext =
        PersistenceContextImpl(
            submissionDataRepository,
            sequenceRepository,
            tagsDataRepository,
            lockExecutor,
            userDataRepository,
            dbSubmissionMapper,
            toExtSubmissionMapper,
            filePersistenceService
        )

    @Bean
    fun submissionQueryService(): SubmissionQueryService =
        SubmissionSqlQueryService(submissionDataRepository, folderResolver)
}
