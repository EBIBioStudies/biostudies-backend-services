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
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.JdbcLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.service.ProjectRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.persistence.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.service.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.service.filesystem.FilesService
import ac.uk.ebi.biostd.persistence.service.filesystem.FtpFilesService
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import uk.ac.ebi.stats.persistence.repositories.SubmissionStatsRepository

@Suppress("TooManyFunctions")
@Configuration
@EnableJpaRepositories(
    repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean::class,
    basePackageClasses = [
        SubmissionDataRepository::class,
        SubmissionRtRepository::class,
        SubmissionStatsRepository::class])
@EntityScan(basePackages = [
    "ac.uk.ebi.biostd.persistence.model",
    "ebi.ac.uk.notifications.persistence.model",
    "uk.ac.ebi.stats.persistence.model"])
class PersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val template: NamedParameterJdbcTemplate,
    private val userDataRepository: UserDataDataRepository,
    private val applicationProperties: ApplicationProperties,
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService,
    private val permissionRepository: AccessPermissionRepository
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
    fun submissionRepository(toExtSubmissionMapper: ToExtSubmissionMapper) =
        SubmissionRepository(submissionDataRepository, submissionDbMapper(), toExtSubmissionMapper)

    @Bean
    fun projectRepository() = ProjectRepository(submissionDataRepository)

    @Bean
    fun submissionDbMapper() = SubmissionDbMapper()

    @Bean
    fun ftpFilesService() = FtpFilesService(folderResolver)

    @Bean
    fun filePersistenceService() = FilesService(folderResolver, serializationService)

    @Bean
    fun userPermissionsService() = UserPermissionsService(permissionRepository)

    @Bean
    fun fileSystemService(filesService: FilesService, ftpService: FtpFilesService) =
        FileSystemService(filesService, ftpService)

    @Bean
    @ConditionalOnMissingBean(LockExecutor::class)
    fun lockExecutor(): LockExecutor = JdbcLockExecutor(template)

    @Bean
    fun persistenceContext(
        lockExecutor: LockExecutor,
        dbSubmissionMapper: ToDbSubmissionMapper,
        toExtSubmissionMapper: ToExtSubmissionMapper,
        fileSystemService: FileSystemService
    ): PersistenceContext =
        PersistenceContextImpl(
            submissionDataRepository,
            sequenceRepository,
            tagsDataRepository,
            lockExecutor,
            userDataRepository,
            dbSubmissionMapper,
            toExtSubmissionMapper,
            fileSystemService
        )

    @Bean
    fun submissionQueryService(): SubmissionQueryService =
        SubmissionSqlQueryService(submissionDataRepository, folderResolver)
}
