package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.JdbcLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SectionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRtRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.SubmissionRepository
import ac.uk.ebi.biostd.persistence.service.NotificationsSqlDataService
import ac.uk.ebi.biostd.persistence.service.SubmissionSqlPersistenceService
import ac.uk.ebi.biostd.persistence.service.filesystem.FileSystemService
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.nio.file.Paths

@Suppress("LongParameterList")
@Configuration
@EnableJpaRepositories(
    repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean::class,
    basePackageClasses = [
        SubmissionDataRepository::class,
        SubmissionRtRepository::class,
        SubmissionStatsDataRepository::class])
open class SqlPersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository,
    private val sectionRepository: SectionDataRepository,
    private val statsRepository: SubmissionStatsDataRepository,
    private val accessTagDataRepo: AccessTagDataRepo,
    private val sequenceRepository: SequenceDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val template: NamedParameterJdbcTemplate,
    private val folderResolver: SubmissionFolderResolver,
    private val applicationProperties: ApplicationProperties
) {

    @Bean
    @ConditionalOnMissingBean(LockExecutor::class)
    open fun lockExecutor(): LockExecutor = JdbcLockExecutor(template)

    @Bean
    open fun notificationsDataService(submissionRtRepository: SubmissionRtRepository): NotificationsDataService =
        NotificationsSqlDataService(submissionRtRepository)

    @Bean
    internal open fun toExtSubmissionMapper() =
        ToExtSubmissionMapper(Paths.get(applicationProperties.submissionPath))

    @Bean
    internal open fun submissionRepository(toExtSubmissionMapper: ToExtSubmissionMapper) =
        SubmissionRepository(submissionDataRepository, sectionRepository, statsRepository, toExtSubmissionMapper())

    @Bean
    internal open fun submissionQueryService(): SubmissionQueryService =
        SubmissionSqlQueryService(submissionDataRepository, accessTagDataRepo, folderResolver)

    @Bean
    internal open fun persistenceService(
        submissionPersistenceService: SubmissionSqlPersistenceService,
        lockExecutor: LockExecutor,
        dbSubmissionMapper: ToDbSubmissionMapper,
        toExtSubmissionMapper: ToExtSubmissionMapper,
        fileSystemService: FileSystemService
    ): PersistenceService =
        SqlPersistenceService(
            submissionPersistenceService,
            sequenceRepository,
            tagsDataRepository,
            lockExecutor
        )

    @Bean
    internal open fun submissionPersistenceService(
        subRepository: SubmissionRepository,
        subDataRepository: SubmissionDataRepository,
        userDataRepository: UserDataDataRepository,
        systemService: FileSystemService,
        toExtMapper: ToExtSubmissionMapper,
        toDbSubmissionMapper: ToDbSubmissionMapper
    ) = SubmissionSqlPersistenceService(
        subRepository,
        subDataRepository,
        userDataRepository,
        systemService,
        toDbSubmissionMapper
    )

    @Bean
    internal open fun toDbSubmissionMapper(
        tagsRepo: AccessTagDataRepo,
        tagsRefRepo: TagDataRepository,
        userRepo: UserDataRepository
    ) = ToDbSubmissionMapper(tagsRepo, tagsRefRepo, userRepo)
}
