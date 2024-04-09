package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.security.integration.SecurityModuleConfig
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.events.service.EventsPublisherService

@Configuration
@Import(value = [FilePersistenceConfig::class, JmsPublishingConfig::class])
@Suppress("TooManyFunctions")
class SecurityConfig(properties: ApplicationProperties) {
    private val securityProps = properties.security

    @Bean
    @SuppressWarnings("LongParameterList")
    fun securityModuleConfig(
        userDataRepository: UserDataRepository,
        queryService: SubmissionMetaQueryService,
        tokenRepository: TokenDataRepository,
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
            eventsPublisherService,
            securityProps,
            clusterClient,
        )

    @Bean
    fun securityService(securityConfig: SecurityModuleConfig): ISecurityService = securityConfig.securityService()

    @Bean
    fun securityQueryService(securityConfig: SecurityModuleConfig): ISecurityQueryService = securityConfig.securityQueryService()

    @Bean
    fun groupService(securityConfig: SecurityModuleConfig): IGroupService = securityConfig.groupService()

    @Bean
    fun userPrivilegesService(securityConfig: SecurityModuleConfig): IUserPrivilegesService = securityConfig.userPrivilegesService()

    @Bean
    fun securityFilter(securityConfig: SecurityModuleConfig): ISecurityFilter = securityConfig.securityFilter()
}
