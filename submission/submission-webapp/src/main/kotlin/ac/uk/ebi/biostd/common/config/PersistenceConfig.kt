package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.PersistenceContextImpl
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.integration.SubmissionSqlQueryService
import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.JdbcLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SectionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.ProjectRepository
import ac.uk.ebi.biostd.persistence.repositories.data.SubmissionRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionPersistenceService
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
import java.nio.file.Paths

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
    "ebi.ac.uk.notifications.persistence.model"])
class PersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository,
    private val sectionRepository: SectionDataRepository,
    private val statsRepository: SubmissionStatsRepository,
    private val accessTagDataRepo: AccessTagDataRepo,
    private val sequenceRepository: SequenceDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val template: NamedParameterJdbcTemplate,
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
    fun toExtSubmissionMapper() =
        ToExtSubmissionMapper(Paths.get(applicationProperties.submissionPath))

    @Bean
    fun submissionRepository(toExtSubmissionMapper: ToExtSubmissionMapper) =
        SubmissionRepository(submissionDataRepository, sectionRepository, statsRepository, toExtSubmissionMapper())

    @Bean
    fun projectRepository() = ProjectRepository(submissionDataRepository)

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
        submissionPersistenceService: SubmissionPersistenceService,
        lockExecutor: LockExecutor,
        dbSubmissionMapper: ToDbSubmissionMapper,
        toExtSubmissionMapper: ToExtSubmissionMapper,
        fileSystemService: FileSystemService
    ): PersistenceContext =
        PersistenceContextImpl(
            submissionPersistenceService,
            sequenceRepository,
            tagsDataRepository,
            lockExecutor
        )

    @Bean
    @Suppress("LongParameterList")
    fun submissionPersistenceService(
        subRepository: SubmissionRepository,
        subDataRepository: SubmissionDataRepository,
        userDataRepository: UserDataDataRepository,
        systemService: FileSystemService,
        toExtMapper: ToExtSubmissionMapper,
        toDbSubmissionMapper: ToDbSubmissionMapper
    ) = SubmissionPersistenceService(
        subRepository,
        subDataRepository,
        userDataRepository,
        systemService,
        toDbSubmissionMapper
    )

    @Bean
    fun submissionQueryService(): SubmissionQueryService =
        SubmissionSqlQueryService(submissionDataRepository, accessTagDataRepo, folderResolver)
}
