package ebi.ac.uk.security.integration

import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.security.events.Events
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserPreRegister
import ebi.ac.uk.security.integration.model.events.UserRegister
import ebi.ac.uk.security.service.GroupService
import ebi.ac.uk.security.service.SecurityService
import ebi.ac.uk.security.util.SecurityUtil
import ebi.ac.uk.security.web.SecurityFilter
import io.jsonwebtoken.Jwts
import io.reactivex.Observable

class SecurityModuleConfig(
    private val userRepository: UserDataRepository,
    private val tokenRepository: TokenDataRepository,
    private val groupRepository: UserGroupDataRepository,
    private var properties: SecurityProperties
) {
    fun securityService(): ISecurityService = securityService
    fun groupService(): IGroupService = groupService
    fun securityFilter(): ISecurityFilter = securityFilter

    val userRegister: Observable<UserRegister> = Events.userRegister
    val passwordReset: Observable<PasswordReset> = Events.passwordReset
    val userPreRegister: Observable<UserPreRegister> = Events.userPreRegister

    private val securityService: SecurityService
        by lazy { SecurityService(userRepository, tokenRepository, securityUtil, properties) }
    private val groupService: GroupService by lazy { GroupService(groupRepository, userRepository) }
    private val securityFilter: SecurityFilter by lazy { SecurityFilter(properties.environment, securityService) }

    private val securityUtil by lazy { SecurityUtil(jwtParser, objectMapper, userRepository, properties.tokenHash) }
    private val objectMapper by lazy { JacksonFactory.createMapper() }
    private val jwtParser by lazy { Jwts.parser()!! }
}
