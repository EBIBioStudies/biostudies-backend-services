package ac.uk.ebi.biostd.persistence.integration.config

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.integration.services.SqlPersistenceService
import ac.uk.ebi.biostd.persistence.integration.services.UserSqlPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.JdbcLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Configuration
@Import(JpaRepositoryConfig::class)
@Suppress("TooManyFunctions", "LongParameterList")
open class SqlPersistenceConfig {
    @Bean
    internal open fun lockExecutor(
        namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    ): LockExecutor = JdbcLockExecutor(namedParameterJdbcTemplate)

    @Bean
    internal open fun sqlPersistenceService(
        sequenceRepository: SequenceDataRepository,
        accessTagsDataRepository: AccessTagDataRepo,
        lockExecutor: LockExecutor,
        submissionQueryService: SubmissionQueryService
    ): PersistenceService =
        SqlPersistenceService(
            sequenceRepository,
            accessTagsDataRepository,
            lockExecutor,
            submissionQueryService
        )

    @Bean
    internal open fun userPermissionsService(
        permissionRepo: AccessPermissionRepository
    ): UserPermissionsService = UserSqlPermissionsService(permissionRepo)
}
