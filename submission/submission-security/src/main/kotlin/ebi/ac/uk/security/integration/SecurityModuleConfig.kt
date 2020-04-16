package ebi.ac.uk.security.integration

import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.security.events.Events
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserRegister
import ebi.ac.uk.security.service.GroupService
import ebi.ac.uk.security.service.ProfileService
import ebi.ac.uk.security.service.SecurityService
import ebi.ac.uk.security.service.UserPrivilegesService
import ebi.ac.uk.security.util.SecurityUtil
import ebi.ac.uk.security.web.SecurityFilter
import io.jsonwebtoken.Jwts
import io.reactivex.Observable
import java.nio.file.Paths

class SecurityModuleConfig(
    private val userRepo: UserDataRepository,
    private val tokenRepo: TokenDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val groupRepository: UserGroupDataRepository,
    private val permissionRepository: AccessPermissionRepository,
    private val queryService: SubmissionQueryService,
    private var props: SecurityProperties
) {
    fun securityService(): ISecurityService = securityService
    fun groupService(): IGroupService = groupService
    fun securityFilter(): ISecurityFilter = securityFilter
    fun userPrivilegesService(): IUserPrivilegesService = userPrivilegesService

    val passwordReset: Observable<PasswordReset> = Events.passwordReset
    val userRegister: Observable<UserRegister> = Events.userPreRegister

    private val groupService by lazy { GroupService(groupRepository, userRepo) }
    private val securityService by lazy { SecurityService(userRepo, securityUtil, props, profileService) }
    private val securityFilter by lazy { SecurityFilter(props.environment, securityService) }
    private val userPrivilegesService by lazy {
        UserPrivilegesService(userRepo, tagsDataRepository, queryService, permissionRepository)
    }

    private val securityUtil by lazy { SecurityUtil(jwtParser, objectMapper, tokenRepo, userRepo, props.tokenHash) }
    private val objectMapper by lazy { JacksonFactory.createMapper() }
    private val jwtParser by lazy { Jwts.parser()!! }
    private val profileService by lazy { ProfileService(Paths.get(props.filesDirPath)) }
}
