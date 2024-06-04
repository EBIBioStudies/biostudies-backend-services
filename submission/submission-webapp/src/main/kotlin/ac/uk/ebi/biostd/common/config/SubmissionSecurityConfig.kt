package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.security.domain.service.ExtUserService
import ac.uk.ebi.biostd.security.domain.service.PermissionService
import ac.uk.ebi.biostd.security.web.SecurityMapper
import ac.uk.ebi.biostd.security.web.exception.SecurityAccessDeniedHandler
import ac.uk.ebi.biostd.security.web.exception.SecurityAuthEntryPoint
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SubmissionSecurityConfig(
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun extUserService(userDataRepository: UserDataRepository): ExtUserService = ExtUserService(userDataRepository)

    @Bean
    fun permissionService(
        permissionRepository: AccessPermissionRepository,
        userDataRepository: UserDataRepository,
        accessTagDataRepository: AccessTagDataRepo,
        submissionQueryService: SubmissionMetaQueryService,
    ) = PermissionService(submissionQueryService, permissionRepository, userDataRepository, accessTagDataRepository)

    @Bean
    fun securityMapper() = SecurityMapper()

    @Bean
    fun accessDeniedHandler(): SecurityAccessDeniedHandler = SecurityAccessDeniedHandler(objectMapper)

    @Bean
    fun securityAuthenticationHandler(): SecurityAuthEntryPoint = SecurityAuthEntryPoint(objectMapper)
}
