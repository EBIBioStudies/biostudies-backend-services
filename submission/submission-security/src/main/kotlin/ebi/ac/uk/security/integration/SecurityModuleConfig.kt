package ebi.ac.uk.security.integration

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.service.CaptchaVerifier
import ebi.ac.uk.security.service.GroupService
import ebi.ac.uk.security.service.ProfileService
import ebi.ac.uk.security.service.SecurityService
import ebi.ac.uk.security.service.UserPrivilegesService
import ebi.ac.uk.security.util.SecurityUtil
import ebi.ac.uk.security.web.SecurityFilter
import io.jsonwebtoken.Jwts
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.events.service.EventsPublisherService
import java.nio.file.Paths

class SecurityModuleConfig(
    private val userRepo: UserDataRepository,
    private val tokenRepo: TokenDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val groupRepository: UserGroupDataRepository,
    private val queryService: SubmissionMetaQueryService,
    private val userPermissionsService: UserPermissionsService,
    private val eventsPublisherService: EventsPublisherService,
    private var props: SecurityProperties
) {
    fun securityService(): ISecurityService = securityService
    fun groupService(): IGroupService = groupService
    fun securityFilter(): ISecurityFilter = securityFilter
    fun userPrivilegesService(): IUserPrivilegesService = userPrivilegesService

    private val groupService by lazy { GroupService(groupRepository, userRepo) }
    private val securityService by lazy {
        SecurityService(userRepo, securityUtil, props, profileService, captchaVerifier, eventsPublisherService)
    }

    private val securityFilter by lazy { SecurityFilter(props.environment, securityService) }
    private val userPrivilegesService by lazy {
        UserPrivilegesService(userRepo, tagsDataRepository, queryService, userPermissionsService)
    }

    private val captchaVerifier by lazy { CaptchaVerifier(RestTemplate(), props) }
    private val securityUtil by lazy { SecurityUtil(jwtParser, objectMapper, tokenRepo, userRepo, props.tokenHash) }
    private val objectMapper by lazy { JacksonFactory.createMapper() }
    private val jwtParser by lazy { Jwts.parser()!! }
    private val profileService by lazy { ProfileService(Paths.get(props.filesDirPath)) }
}
