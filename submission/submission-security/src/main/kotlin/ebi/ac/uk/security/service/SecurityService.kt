package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.ext.activated
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import arrow.core.getOrElse
import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.GetOrRegisterUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.security.events.Events
import ebi.ac.uk.security.events.Events.userPreRegister
import ebi.ac.uk.security.events.Events.userRegister
import ebi.ac.uk.security.integration.SecurityProperties
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserNotFoundByTokenException
import ebi.ac.uk.security.integration.exception.UserPendingRegistrationException
import ebi.ac.uk.security.integration.exception.UserWithActivationKeyNotFoundException
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserActivated
import ebi.ac.uk.security.integration.model.events.UserRegister
import ebi.ac.uk.security.util.SecurityUtil

@Suppress("TooManyFunctions")
internal class SecurityService(
    private val userRepository: UserDataRepository,
    private val securityUtil: SecurityUtil,
    private val securityProps: SecurityProperties,
    private val profileService: ProfileService
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
        return when {
            userRepository.existsByEmail(request.email) -> throw UserAlreadyRegister(request.email)
            securityProps.requireActivation -> register(request)
            else -> activate(request)
        }
    }

    override fun getOrCreateInactive(email: String, username: String): SecurityUser {
        return userRepository.findByEmail(email)
            .orElseGet { createUserInactive(email, username) }
            .let { profileService.asSecurityUser(it) }
    }

    override fun getOrRegisterUser(request: GetOrRegisterUserRequest): SecurityUser {
        return when (request.register) {
            false -> userRepository.getByEmail(request.userEmail).let { profileService.asSecurityUser(it) }
            true -> {
                val userName = requireNotNull(request.userName) { " username need to be provided for registering" }
                getOrCreateInactive(request.userEmail, userName)
            }
        }
    }

    private fun createUserInactive(email: String, username: String): DbUser {
        val user = DbUser(
            email = email,
            fullName = username,
            secret = securityUtil.newKey(),
            passwordDigest = ByteArray(0))
        user.active = false
        user.notificationsEnabled = false
        return userRepository.save(user)
    }

    override fun getUser(email: String): SecurityUser {
        return userRepository.findByEmailAndActive(email, true)
            .map { profileService.asSecurityUser(it) }
            .orElseThrow { throw UserAlreadyRegister(email) }
    }

    override fun activate(activationKey: String) {
        val user = userRepository.findByActivationKeyAndActive(activationKey, false)
            .orElseThrow { UserWithActivationKeyNotFoundException() }
        user.activationKey = null
        user.active = true
        userRepository.save(user)
    }

    override fun retryRegistration(request: RetryActivationRequest) {
        val user = userRepository.findByEmailAndActive(request.email, false)
            .orElseThrow { UserPendingRegistrationException(request.email) }
        register(user, request.instanceKey, request.path)
    }

    override fun changePassword(request: ChangePasswordRequest) {
        val user = userRepository.findByActivationKeyAndActive(request.activationKey, true)
            .orElseThrow { UserWithActivationKeyNotFoundException() }
        user.activationKey = null
        user.passwordDigest = securityUtil.getPasswordDigest(request.password)
        userRepository.save(user)
    }

    override fun resetPassword(request: ResetPasswordRequest) {
        val email = request.email
        val user = userRepository.findByLoginOrEmailAndActive(email, email, true)
            .orElseThrow { UserNotFoundByEmailException(email) }
        val key = securityUtil.newKey()
        userRepository.save(user.apply { activationKey = key })
        val resetUrl = securityUtil.getActivationUrl(request.instanceKey, request.path, key)
        Events.passwordReset.onNext(PasswordReset(user, resetUrl))
    }

    override fun getUserProfile(authToken: String): UserInfo {
        return securityUtil.checkToken(authToken)
            .getOrElse { throw UserNotFoundByTokenException() }
            .let { profileService.getUserProfile(it, authToken) }
    }

    private fun register(request: RegisterRequest): SecurityUser {
        val instanceKey = checkNotNull(request.instanceKey) { "Instance key can not be null when activation" }
        val activationPath = checkNotNull(request.path) { "Activation path can not be null" }
        return register(asUser(request), instanceKey, activationPath)
    }

    private fun register(user: DbUser, instanceKey: String, activationPath: String): SecurityUser {
        val key = securityUtil.newKey()
        val saved = userRepository.save(user.apply { user.activationKey = key })
        userPreRegister.onNext(UserRegister(saved, securityUtil.getActivationUrl(instanceKey, activationPath, key)))
        return profileService.asSecurityUser(saved)
    }

    private fun activate(request: RegisterRequest): SecurityUser {
        val user = userRepository.save(asUser(request).activated())
        userRegister.onNext(UserActivated(user))
        return profileService.asSecurityUser(user)
    }

    private fun asUser(registerRequest: RegisterRequest): DbUser {
        return DbUser(
            email = registerRequest.email,
            fullName = registerRequest.name,
            secret = securityUtil.newKey(),
            superuser = registerRequest.superUser,
            notificationsEnabled = registerRequest.notificationsEnabled,
            passwordDigest = securityUtil.getPasswordDigest(registerRequest.password))
    }
}
