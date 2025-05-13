package ebi.ac.uk.security.integration

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.components.SecurityFilter
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.service.CaptchaVerifier
import ebi.ac.uk.security.service.GroupService
import ebi.ac.uk.security.service.ProfileService
import ebi.ac.uk.security.service.SecurityService
import ebi.ac.uk.security.service.SqlSecurityQueryService
import ebi.ac.uk.security.service.UserPrivilegesService
import ebi.ac.uk.security.util.SecurityUtil
import ebi.ac.uk.security.web.SpringSecurityFilter
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.events.service.EventsPublisherService
import java.nio.file.Paths

@Suppress("LongParameterList")
class SecurityModuleConfig(
    private val userRepo: UserDataRepository,
    private val tokenRepo: TokenDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val groupRepository: UserGroupDataRepository,
    private val queryService: SubmissionMetaQueryService,
    private val userPermissionsService: UserPermissionsService,
    private val eventsPublisherService: EventsPublisherService,
    private val props: SecurityProperties,
    private val clusterClient: ClusterClient,
) {
    fun securityService(): ISecurityService = securityService

    fun securityQueryService(): SecurityQueryService = securityQueryService

    fun groupService(): IGroupService = groupService

    fun securityFilter(): SecurityFilter = securityFilter

    fun userPrivilegesService(): IUserPrivilegesService = userPrivilegesService

    private val groupService by lazy { GroupService(groupRepository, userRepo, props.filesProperties.filesDirPath) }
    private val securityService by lazy {
        SecurityService(
            userRepo,
            securityUtil,
            props,
            profileService,
            captchaVerifier,
            eventsPublisherService,
            securityQueryService,
            clusterClient,
        )
    }
    private val securityQueryService by lazy {
        SqlSecurityQueryService(
            securityUtil,
            profileService,
            userRepo,
            props,
        )
    }

    private val securityFilter by lazy { SpringSecurityFilter(props.environment, securityQueryService) }
    private val userPrivilegesService by lazy {
        UserPrivilegesService(userRepo, tagsDataRepository, queryService, userPermissionsService)
    }

    private val captchaVerifier by lazy { CaptchaVerifier(WebClient.builder().build(), props) }
    private val objectMapper by lazy { JacksonFactory.createMapper() }
    private val jwtParser by lazy { Jwts.parser()!! }
    private val profileService by lazy { profileService(props) }
    private val securityUtil by lazy { securityUtil(jwtParser, objectMapper, tokenRepo, userRepo, props) }

    companion object {
        fun securityUtil(
            jwtParser: JwtParser,
            objectMapper: ObjectMapper,
            tokenRepo: TokenDataRepository,
            userRepo: UserDataRepository,
            props: SecurityProperties,
        ): SecurityUtil = SecurityUtil(jwtParser, objectMapper, tokenRepo, userRepo, props.tokenHash, props.instanceKeys)

        fun profileService(props: SecurityProperties): ProfileService =
            ProfileService(
                userFtpRootPath = props.filesProperties.userFtpRootPath,
                userFtpDirPath = Paths.get(props.filesProperties.userFtpDirPath),
                nfsUserFilesDirPath = Paths.get(props.filesProperties.filesDirPath),
            )
    }
}
