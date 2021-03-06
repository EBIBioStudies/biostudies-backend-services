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
import ebi.ac.uk.extended.events.SecurityNotificationType
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION_BY_EMAIL
import ebi.ac.uk.extended.events.SecurityNotificationType.PASSWORD_RESET
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RWX__X___
import ebi.ac.uk.model.User
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserPendingRegistrationException
import ebi.ac.uk.security.integration.exception.UserWithActivationKeyNotFoundException
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.util.SecurityUtil
import uk.ac.ebi.events.service.EventsPublisherService
import java.nio.file.Path
import java.nio.file.Paths

@Suppress("TooManyFunctions")
class SecurityService(
    private val userRepository: UserDataRepository,
    private val securityUtil: SecurityUtil,
    private val securityProps: SecurityProperties,
    private val profileService: ProfileService,
    private val captchaVerifier: CaptchaVerifier,
    private val eventsPublisherService: EventsPublisherService
) : ISecurityService {
    override fun login(request: LoginRequest): UserInfo =
        userRepository
            .findByLoginOrEmailAndActive(request.login, request.login, true)
            .filter { securityUtil.checkPassword(it.passwordDigest, request.password) }
            .orElseThrow { LoginException() }
            .let { profileService.getUserProfile(it, securityUtil.createToken(it)) }

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
        val user = userRepository.findByEmailAndActive(email, true)
            .map { activate(it) }
            .orElseThrow { UserNotFoundByEmailException(email) }

        FileUtils.setFolderPermissions(user.magicFolder.path.parent, RWX__X___)
        FileUtils.setFolderPermissions(user.magicFolder.path, RWXRWX___)
        return user
    }

    override fun activate(activationKey: String) {
        userRepository.findByActivationKeyAndActive(activationKey, false)
            .map(this::activate)
            .orElseThrow(::UserWithActivationKeyNotFoundException)
    }

    override fun activateByEmail(request: ActivateByEmailRequest) {
        val email = request.email
        userRepository
            .findByEmailAndActive(email, false)
            .map(this::activate)
            .orElseThrow { UserNotFoundByEmailException(email) }

        resetNotification(email, request.instanceKey, request.path, ACTIVATION_BY_EMAIL)
    }

    override fun retryRegistration(request: RetryActivationRequest) {
        val user = userRepository.findByEmailAndActive(request.email, false)
            .orElseThrow { UserPendingRegistrationException(request.email) }
        register(user, request.instanceKey, request.path)
    }

    override fun changePassword(request: ChangePasswordRequest): User {
        val user = userRepository
            .findByActivationKeyAndActive(request.activationKey, true)
            .orElseThrow { UserWithActivationKeyNotFoundException() }

        user.activationKey = null
        user.passwordDigest = securityUtil.getPasswordDigest(request.password)

        val updatedPassword = userRepository.save(user)
        return profileService.asSecurityUser(updatedPassword).asUser()
    }

    override fun resetPassword(request: ResetPasswordRequest) {
        if (securityProps.checkCaptcha) captchaVerifier.verifyCaptcha(request.captcha)
        resetNotification(request.email, request.instanceKey, request.path, PASSWORD_RESET)
    }

    private fun resetNotification(email: String, instanceKey: String, path: String, type: SecurityNotificationType) {
        val user = userRepository
            .findByLoginOrEmailAndActive(email, email, true)
            .orElseThrow { UserNotFoundByEmailException(email) }
        val key = securityUtil.newKey()
        userRepository.save(user.apply { activationKey = key })

        val resetUrl = securityUtil.getActivationUrl(instanceKey, path, key)
        val resetNotification = SecurityNotification(user.email, user.fullName, resetUrl, type)

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
        val notification = SecurityNotification(saved.email, saved.fullName, activationUrl, ACTIVATION)
        eventsPublisherService.securityNotification(notification)
        return saved
    }

    private fun activate(toActivate: DbUser): SecurityUser {
        val dbUser = userRepository.save(toActivate.apply { activationKey = null; active = true })
        val securityUser = profileService.asSecurityUser(dbUser)

        FileUtils.getOrCreateFolder(securityUser.magicFolder.path.parent, RWX__X___)
        FileUtils.getOrCreateFolder(securityUser.magicFolder.path, RWXRWX___)
        FileUtils.createSymbolicLink(symLinkPath(securityUser.email), securityUser.magicFolder.path, RWXRWX___)
        return securityUser
    }

    private fun symLinkPath(userEmail: String): Path {
        val prefixFolder = userEmail.substring(0, 1).toLowerCase()
        return Paths.get("${securityProps.magicDirPath}/$prefixFolder/$userEmail")
    }

    private fun asUser(registerRequest: RegisterRequest) = DbUser(
        email = registerRequest.email,
        fullName = registerRequest.name,
        secret = securityUtil.newKey(),
        notificationsEnabled = registerRequest.notificationsEnabled,
        passwordDigest = securityUtil.getPasswordDigest(registerRequest.password)
    )
}
