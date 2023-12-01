package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.api.security.ActivateByEmailRequest
import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION_BY_EMAIL
import ebi.ac.uk.extended.events.SecurityNotificationType.PASSWORD_RESET
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RWX__X___
import ebi.ac.uk.model.User
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.exception.ActKeyNotFoundException
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserPendingRegistrationException
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.persistence.getActiveByEmail
import ebi.ac.uk.security.persistence.getActiveByLoginOrEmail
import ebi.ac.uk.security.persistence.getByActivationKey
import ebi.ac.uk.security.persistence.getInactiveByActivationKey
import ebi.ac.uk.security.persistence.getInactiveByEmail
import ebi.ac.uk.security.util.SecurityUtil
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional
import uk.ac.ebi.biostd.client.cluster.api.ClusterOperations
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.events.service.EventsPublisherService
import java.nio.file.Path
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions", "LongParameterList")
@Transactional
open class SecurityService(
    private val userRepository: UserDataRepository,
    private val securityUtil: SecurityUtil,
    private val props: SecurityProperties,
    private val profileService: ProfileService,
    private val captchaVerifier: CaptchaVerifier,
    private val eventsPublisherService: EventsPublisherService,
    private val clusterClient: ClusterOperations,
) : ISecurityService {
    override fun login(request: LoginRequest): UserInfo {
        val user = userRepository.getActiveByLoginOrEmail(request.login)
        require(securityUtil.checkPassword(user.passwordDigest, request.password)) { throw LoginException() }
        return profileService.getUserProfile(user, securityUtil.createToken(user))
    }

    override fun logout(authToken: String) {
        securityUtil.invalidateToken(authToken)
    }

    override fun registerUser(request: RegisterRequest): SecurityUser {
        if (props.checkCaptcha) captchaVerifier.verifyCaptcha(request.captcha)

        return when {
            userRepository.existsByEmail(request.email) -> throw UserAlreadyRegister(request.email)
            props.requireActivation -> register(request)
            else -> activate(asUser(request))
        }
    }

    override fun refreshUser(email: String): SecurityUser {
        val user = userRepository.getActiveByEmail(email)
        return runBlocking { activate(user) }
    }

    override fun activate(activationKey: String) {
        val user = userRepository.getInactiveByActivationKey(activationKey)
        runBlocking { activate(user) }
    }

    override fun activateByEmail(request: ActivateByEmailRequest) {
        val (email, instanceKey, path) = request
        val user = userRepository.getInactiveByEmail(email)

        val activationKey = user.activationKey ?: throw ActKeyNotFoundException()
        val activationUrl = securityUtil.getActivationUrl(instanceKey, path, activationKey)
        val notification = SecurityNotification(email, user.fullName, activationKey, activationUrl, ACTIVATION_BY_EMAIL)
        eventsPublisherService.securityNotification(notification)
    }

    override fun retryRegistration(request: RetryActivationRequest) {
        val user = userRepository.findByEmailAndActive(request.email, false)
            ?: throw UserPendingRegistrationException(request.email)
        register(user, request.instanceKey, request.path)
    }

    override fun activateAndSetupPassword(request: ChangePasswordRequest): User {
        val user = userRepository.getInactiveByActivationKey(request.activationKey)
        runBlocking { activate(user) }
        return setPassword(user, request.password)
    }

    override fun changePassword(request: ChangePasswordRequest): User {
        val user = userRepository.getByActivationKey(request.activationKey)
        runBlocking { activate(user) }

        return setPassword(user, request.password)
    }

    override fun resetPassword(request: ResetPasswordRequest) {
        if (props.checkCaptcha) captchaVerifier.verifyCaptcha(request.captcha)
        resetNotification(request.email, request.instanceKey, request.path)
    }

    private fun setPassword(user: DbUser, password: String): User {
        user.passwordDigest = securityUtil.getPasswordDigest(password)

        val updatedPassword = userRepository.save(user)
        return profileService.asSecurityUser(updatedPassword).asUser()
    }

    private fun resetNotification(email: String, instanceKey: String, path: String) {
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundByEmailException(email)
        val key = securityUtil.newKey()
        userRepository.save(user.apply { activationKey = key })

        val resetUrl = securityUtil.getActivationUrl(instanceKey, path, key)
        val resetNotification = SecurityNotification(user.email, user.fullName, key, resetUrl, PASSWORD_RESET)

        eventsPublisherService.securityNotification(resetNotification)
    }

    private fun register(request: RegisterRequest): SecurityUser {
        val instanceKey = checkNotNull(request.instanceKey) { "Instance key can not be null when activation" }
        val activationPath = checkNotNull(request.path) { "Activation path can not be null" }
        return register(asUser(request), instanceKey, activationPath)
    }

    private fun register(user: DbUser, instanceKey: String, activationPath: String): SecurityUser {
        val dbUser = registerUser(user, instanceKey, activationPath)
        return profileService.asSecurityUser(dbUser)
    }

    private fun registerUser(user: DbUser, instanceKey: String, activationPath: String): DbUser {
        val key = securityUtil.newKey()
        val saved = userRepository.save(user.apply { user.activationKey = key })
        val activationUrl = securityUtil.getActivationUrl(instanceKey, activationPath, key)
        val notification = SecurityNotification(saved.email, saved.fullName, key, activationUrl, ACTIVATION)
        eventsPublisherService.securityNotification(notification)
        return saved
    }

    private fun activate(toActivate: DbUser): SecurityUser {
        val dbUser = userRepository.save(toActivate.apply { activationKey = null; active = true })
        val securityUser = profileService.asSecurityUser(dbUser)

        runBlocking { createMagicFolder(securityUser) }
        return securityUser
    }

    private suspend fun createMagicFolder(user: SecurityUser) {
        when (user.userFolder) {
            is FtpUserFolder -> createFtpMagicFolder(user.userFolder)
            is NfsUserFolder -> createNfsMagicFolder(user.email, user.userFolder)
        }
    }

    private suspend fun createFtpMagicFolder(ftpFolder: FtpUserFolder) {
        createClusterFolder(ftpFolder.path.parent, UNIX_RWX__X___)
        createClusterFolder(ftpFolder.path, UNIX_RWXRWX___)
    }

    private suspend fun createClusterFolder(path: Path, permissions: Int) {
        val command = String.format(CREATE_FOLDER_COMMAND, permissions, path)
        val job = JobSpec(queue = DataMoverQueue, command = command)

        logger.info { "Started creating the cluster FTP folder $path" }
        clusterClient.awaitJob(job)
        logger.info { "Finished creating the cluster FTP folder $path" }
    }

    private fun createNfsMagicFolder(email: String, nfsFolder: NfsUserFolder) {
        FileUtils.getOrCreateFolder(nfsFolder.path.parent, RWX__X___)
        FileUtils.getOrCreateFolder(nfsFolder.path, RWXRWX___)
        FileUtils.createSymbolicLink(symLinkPath(email), nfsFolder.path, RWXRWX___)
    }

    private fun symLinkPath(userEmail: String): Path {
        val prefixFolder = userEmail.substring(0, 1).lowercase()
        return Paths.get("${props.filesProperties.magicDirPath}/$prefixFolder/$userEmail")
    }

    private fun asUser(rqt: RegisterRequest) = DbUser(
        email = rqt.email.lowercase(),
        fullName = rqt.name,
        orcid = rqt.orcid,
        secret = securityUtil.newKey(),
        notificationsEnabled = rqt.notificationsEnabled,
        storageMode = rqt.storageMode?.let { StorageMode.valueOf(it) } ?: props.filesProperties.defaultMode,
        passwordDigest = securityUtil.getPasswordDigest(rqt.password)
    )

    companion object {
        internal const val UNIX_RWX__X___ = 710
        internal const val UNIX_RWXRWX___ = 770
        internal const val CREATE_FOLDER_COMMAND = "mkdir -m %d -p %s"
    }
}
