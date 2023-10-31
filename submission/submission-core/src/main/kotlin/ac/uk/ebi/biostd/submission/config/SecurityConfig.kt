package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ac.uk.ebi.biostd.security.domain.service.ExtUserService
import ac.uk.ebi.biostd.security.domain.service.PermissionService
import ac.uk.ebi.biostd.security.web.SecurityMapper
import ac.uk.ebi.biostd.security.web.exception.SecurityAccessDeniedHandler
import ac.uk.ebi.biostd.security.web.exception.SecurityAuthEntryPoint
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.security.integration.SecurityModuleConfig
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.events.service.EventsPublisherService

@Configuration
@Import(FilePersistenceConfig::class)
@Suppress("TooManyFunctions")
class SecurityConfig(private val objectMapper: ObjectMapper, properties: ApplicationProperties) {
    private val securityProps = properties.security

    @Bean
    fun securityMapper() = SecurityMapper()

    @Bean
    fun accessDeniedHandler(): SecurityAccessDeniedHandler = SecurityAccessDeniedHandler(objectMapper)

    @Bean
    fun securityAuthenticationHandler(): SecurityAuthEntryPoint = SecurityAuthEntryPoint(objectMapper)

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
    ): SecurityModuleConfig = SecurityModuleConfig(
        userDataRepository,
        tokenRepository,
        tagsRepository,
        groupRepository,
        queryService,
        userPermissionsService,
        eventsPublisherService,
        securityProps
    )

    @Bean
    fun securityService(securityConfig: SecurityModuleConfig): ISecurityService = securityConfig.securityService()

    @Bean
    fun securityQueryService(
        securityConfig: SecurityModuleConfig,
    ): ISecurityQueryService = securityConfig.securityQueryService()

    @Bean
    fun groupService(securityConfig: SecurityModuleConfig): IGroupService = securityConfig.groupService()

    @Bean
    fun userPrivilegesService(securityConfig: SecurityModuleConfig): IUserPrivilegesService =
        securityConfig.userPrivilegesService()

    @Bean
    fun securityFilter(securityConfig: SecurityModuleConfig): ISecurityFilter = securityConfig.securityFilter()

    @Bean
    fun extUserService(userDataRepository: UserDataRepository): ExtUserService = ExtUserService(userDataRepository)

    @Bean
    fun permissionService(
        permissionRepository: AccessPermissionRepository,
        userDataRepository: UserDataRepository,
        accessTagDataRepository: AccessTagDataRepo,
    ) = PermissionService(permissionRepository, userDataRepository, accessTagDataRepository)
}
