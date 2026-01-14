package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ac.uk.ebi.biostd.submission.domain.security.LocalUserFolderService
import ac.uk.ebi.biostd.submission.domain.security.RemoteUserFolderService
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor
import ebi.ac.uk.security.integration.SecurityModuleConfig
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.components.SecurityFilter
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.service.ProfileService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.events.service.EventsPublisherService

@Configuration
@Import(value = [FilePersistenceConfig::class, JmsPublishingConfig::class])
@Suppress("TooManyFunctions")
class SecurityConfig(
    properties: ApplicationProperties,
) {
    private val securityProps = properties.security

    @Bean
    @SuppressWarnings("LongParameterList")
    fun securityModuleConfig(
        userDataRepository: UserDataRepository,
        queryService: SubmissionMetaQueryService,
        tokenRepository: TokenDataRepository,
        filesPersistenceService: SubmissionFilesPersistenceService,
        tagsRepository: AccessTagDataRepo,
        groupRepository: UserGroupDataRepository,
        userPermissionsService: UserPermissionsService,
        eventsPublisherService: EventsPublisherService,
        clusterClient: ClusterClient,
    ): SecurityModuleConfig =
        SecurityModuleConfig(
            userDataRepository,
            tokenRepository,
            tagsRepository,
            groupRepository,
            queryService,
            userPermissionsService,
            filesPersistenceService,
            eventsPublisherService,
            securityProps,
            clusterClient,
        )

    @Bean
    fun securityQueryService(securityConfig: SecurityModuleConfig): SecurityQueryService =
        securityConfig.securityQueryService()

    @Bean
    fun groupService(securityConfig: SecurityModuleConfig): IGroupService = securityConfig.groupService()

    @Bean
    fun userPrivilegesService(securityConfig: SecurityModuleConfig): IUserPrivilegesService =
        securityConfig.userPrivilegesService()

    @Bean
    fun securityFilter(securityConfig: SecurityModuleConfig): SecurityFilter = securityConfig.securityFilter()

    @Bean
    fun securityService(securityConfig: SecurityModuleConfig): ISecurityService = securityConfig.securityService()

    @Bean
    fun profileService(securityConfig: SecurityModuleConfig): ProfileService = securityConfig.profileService()

    @Bean
    fun localUserFolderService(
        securityQueryService: SecurityQueryService,
        userRepository: UserDataRepository,
        profileService: ProfileService,
    ): LocalUserFolderService {
        return LocalUserFolderService(securityQueryService, userRepository, profileService, securityProps)
    }

    @Bean
    fun remoteUserFolderService(remoteSubmitterExecutor: RemoteSubmitterExecutor): RemoteUserFolderService {
        return RemoteUserFolderService(remoteSubmitterExecutor)
    }
}
