package ac.uk.ebi.biostd.persistence.integration.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.ProjectDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.integration.services.SqlPersistenceService
import ac.uk.ebi.biostd.persistence.integration.services.SqlSubmissionRequestService
import ac.uk.ebi.biostd.persistence.integration.services.SubmissionSqlPersistenceService
import ac.uk.ebi.biostd.persistence.integration.services.SubmissionSqlQueryService
import ac.uk.ebi.biostd.persistence.integration.services.UserSqlPermissionsService
import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.JdbcLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SectionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRequestDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.ProjectSqlDataService
import ac.uk.ebi.biostd.persistence.repositories.data.SubmissionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.nio.file.Paths

@Suppress("LongParameterList")
@Configuration
@Import(JpaRepositoryConfig::class)
open class SqlPersistenceConfig(private val applicationProperties: ApplicationProperties) {
    @Bean
    @ConditionalOnMissingBean(LockExecutor::class)
    internal open fun lockExecutor(
        namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    ): LockExecutor = JdbcLockExecutor(namedParameterJdbcTemplate)

    @Bean
    internal open fun toExtSubmissionMapper(): ToExtSubmissionMapper =
        ToExtSubmissionMapper(Paths.get(applicationProperties.submissionPath))

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enabledMongo"], havingValue = "false")
    internal open fun submissionRepository(
        toExtSubmissionMapper: ToExtSubmissionMapper,
        submissionDataRepository: SubmissionDataRepository,
        sectionRepository: SectionDataRepository,
        statsRepository: SubmissionStatsDataRepository,
        extSerializationService: ExtSerializationService,
        requestDataRepository: SubmissionRequestDataRepository
    ) = SubmissionRepository(
        submissionDataRepository,
        sectionRepository,
        statsRepository,
        requestDataRepository,
        extSerializationService,
        toExtSubmissionMapper())

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enabledMongo"], havingValue = "false")
    internal open fun submissionQueryService(
        submissionDataRepository: SubmissionDataRepository,
        accessTagDataRepo: AccessTagDataRepo
    ): SubmissionMetaQueryService = SubmissionSqlQueryService(submissionDataRepository, accessTagDataRepo)

    @Bean
    internal open fun sqlPersistenceService(
        sequenceRepository: SequenceDataRepository,
        accessTagsDataRepository: AccessTagDataRepo,
        lockExecutor: LockExecutor,
        submissionQueryService: SubmissionQueryService
    ): PersistenceService {
        return SqlPersistenceService(
            sequenceRepository,
            accessTagsDataRepository,
            lockExecutor,
            submissionQueryService
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enabledMongo"], havingValue = "false")
    internal open fun sqlSubmissionRequest(
        submissionPersistenceService: SubmissionSqlPersistenceService,
        lockExecutor: LockExecutor
    ): SubmissionRequestService =
        SqlSubmissionRequestService(submissionPersistenceService, lockExecutor)

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enabledMongo"], havingValue = "false")
    internal open fun submissionPersistenceService(
        subRepository: SubmissionRepository,
        serializationService: ExtSerializationService,
        subDataRepository: SubmissionDataRepository,
        requestDataRepository: SubmissionRequestDataRepository,
        userDataRepository: UserDataDataRepository,
        systemService: FileSystemService,
        toExtMapper: ToExtSubmissionMapper,
        toDbSubmissionMapper: ToDbSubmissionMapper
    ): SubmissionSqlPersistenceService = SubmissionSqlPersistenceService(
        subRepository,
        serializationService,
        subDataRepository,
        requestDataRepository,
        userDataRepository,
        systemService,
        toDbSubmissionMapper)

    @Bean
    internal open fun toDbSubmissionMapper(
        tagsRepo: AccessTagDataRepo,
        tagsRefRepo: TagDataRepository,
        userRepo: UserDataRepository
    ) = ToDbSubmissionMapper(tagsRepo, tagsRefRepo, userRepo)

    @Bean
    internal open fun projectDataService(
        submissionRepository: SubmissionDataRepository
    ): ProjectDataService = ProjectSqlDataService(submissionRepository)

    @Bean
    internal open fun userPermissionsService(
        permissionRepo: AccessPermissionRepository
    ): UserPermissionsService = UserSqlPermissionsService(permissionRepo)
}
