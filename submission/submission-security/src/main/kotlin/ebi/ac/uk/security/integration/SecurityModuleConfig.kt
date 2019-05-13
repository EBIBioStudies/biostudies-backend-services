package ebi.ac.uk.security.integration

import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.service.GroupService
import ebi.ac.uk.security.service.SecurityService
import ebi.ac.uk.security.util.SecurityUtil
import ebi.ac.uk.security.web.SecurityFilter
import io.jsonwebtoken.Jwts

class SecurityModuleConfig(
    private val userRepository: UserDataRepository,
    private val groupRepository: UserGroupDataRepository,
    private var properties: SecurityProperties
) {

    fun securityService(): ISecurityService = SecurityService(userRepository, securityUtil, properties)

    fun groupService(): IGroupService = GroupService(groupRepository, userRepository)

    fun securityFilter(): ISecurityFilter = SecurityFilter(properties.environment, securityUtil)

    private val securityUtil by lazy { SecurityUtil(jwtParser, objectMapper, userRepository, properties.tokenHash) }
    private val objectMapper by lazy { JacksonFactory.createMapper() }
    private val jwtParser by lazy { Jwts.parser()!! }
}
