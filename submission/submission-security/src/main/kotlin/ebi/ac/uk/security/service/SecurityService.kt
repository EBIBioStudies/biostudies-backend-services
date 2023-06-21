package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.SecurityProperties
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
import ebi.ac.uk.security.integration.model.api.FtpMagicFolder
import ebi.ac.uk.security.integration.model.api.NfsMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.persistence.getActiveByEmail
import ebi.ac.uk.security.persistence.getActiveByLoginOrEmail
import ebi.ac.uk.security.persistence.getByActivationKey
import ebi.ac.uk.security.persistence.getInactiveByActivationKey
import ebi.ac.uk.security.persistence.getInactiveByEmail
import ebi.ac.uk.security.util.SecurityUtil
import org.springframework.transaction.annotation.Transactional
import uk.ac.ebi.events.service.EventsPublisherService
import java.nio.file.Path
import java.nio.file.Paths

@Suppress("TooManyFunctions")
@Transactional
open class SecurityService(
    private val userRepository: UserDataRepository,
    private val securityUtil: SecurityUtil,
    private val securityProps: SecurityProperties,
    private val profileService: ProfileService,
    private val captchaVerifier: CaptchaVerifier,
    private val eventsPublisherService: EventsPublisherService,
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
        if (securityProps.checkCaptcha) captchaVerifier.verifyCaptcha(request.captcha)

        return when {
            userRepository.existsByEmail(request.email) -> throw UserAlreadyRegister(request.email)
            securityProps.requireActivation -> register(request)
            else -> activate(asUser(request))
        }
    }

    override fun refreshUser(email: String): SecurityUser {
        val user = userRepository.getActiveByEmail(email)
        return activate(user)
    }

    override fun activate(activationKey: String) {
        val user = userRepository.getInactiveByActivationKey(activationKey)
        activate(user)
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
        activate(user)
        return setPassword(user, request.password)
    }

    override fun changePassword(request: ChangePasswordRequest): User {
        val user = userRepository.getByActivationKey(request.activationKey)
        activate(user)

        return setPassword(user, request.password)
    }

    override fun resetPassword(request: ResetPasswordRequest) {
        if (securityProps.checkCaptcha) captchaVerifier.verifyCaptcha(request.captcha)
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

        createMagicFolder(securityUser)
        return securityUser
    }

    private fun createMagicFolder(user: SecurityUser) {
        when (user.magicFolder) {
            is FtpMagicFolder -> TODO()
            is NfsMagicFolder -> createNfsMagicFolder(user.email, user.magicFolder)
        }
    }

    private fun createNfsMagicFolder(email: String, magicFolder: NfsMagicFolder) {
        FileUtils.getOrCreateFolder(magicFolder.path.parent, RWX__X___)
        FileUtils.getOrCreateFolder(magicFolder.path, RWXRWX___)
        FileUtils.createSymbolicLink(symLinkPath(email), magicFolder.path, RWXRWX___)
    }

    private fun symLinkPath(userEmail: String): Path {
        val prefixFolder = userEmail.substring(0, 1).lowercase()
        return Paths.get("${securityProps.filesProperties.magicDirPath}/$prefixFolder/$userEmail")
    }

    private fun asUser(registerRequest: RegisterRequest) = DbUser(
        email = registerRequest.email,
        fullName = registerRequest.name,
        orcid = registerRequest.orcid,
        secret = securityUtil.newKey(),
        notificationsEnabled = registerRequest.notificationsEnabled,
        storageMode = securityProps.filesProperties.defaultMode,
        passwordDigest = securityUtil.getPasswordDigest(registerRequest.password)
    )
}
