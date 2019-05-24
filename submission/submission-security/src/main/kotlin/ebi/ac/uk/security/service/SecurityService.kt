package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.SecurityToken
import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.model.ext.activated
import ac.uk.ebi.biostd.persistence.model.ext.register
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import arrow.core.Option
import arrow.core.getOrElse
import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.security.events.Events
import ebi.ac.uk.security.events.Events.Companion.userPreRegister
import ebi.ac.uk.security.events.Events.Companion.userRegister
import ebi.ac.uk.security.integration.SecurityProperties
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.exception.ActKeyNotFoundException
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.exception.UserNotFoundException
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserPreRegister
import ebi.ac.uk.security.integration.model.events.UserRegister
import ebi.ac.uk.security.util.SecurityUtil
import java.time.Clock
import java.time.OffsetDateTime

@Suppress("TooManyFunctions")
internal class SecurityService(
    private val userRepository: UserDataRepository,
    private val tokenRepository: TokenDataRepository,
    private val securityUtil: SecurityUtil,
    private val securityProps: SecurityProperties
) : ISecurityService {

    override fun login(request: LoginRequest): UserInfo =
        userRepository
            .findByLoginOrEmailAndActive(request.login, request.login, true)
            .filter { securityUtil.checkPassword(it.passwordDigest, request.password) }
            .orElseThrow { LoginException() }
            .let { UserInfo(it, securityUtil.createToken(it)) }

    override fun logout(authToken: String) {
        tokenRepository.save(SecurityToken(authToken, OffsetDateTime.now(Clock.systemUTC())))
    }

    override fun registerUser(request: RegisterRequest): User {
        return when {
            userRepository.existsByEmail(request.email) -> throw UserAlreadyRegister(request.email)
            securityProps.requireActivation -> preRegister(request)
            else -> register(request)
        }
    }

    override fun activate(activationKey: String) {
        val user = userRepository.findByActivationKeyAndActive(activationKey, false)
            .orElseThrow { ActKeyNotFoundException() }

        user.activationKey = null
        user.active = true
        userRepository.save(user)
    }

    override fun retryRegistration(request: RetryActivationRequest) {
        val user = userRepository.findByEmailAndActive(request.email, false)
            .orElseThrow { UserNotFoundException() }
        preRegister(user, request.instanceKey, request.path)
    }

    override fun changePassword(request: ChangePasswordRequest) {
        val user = userRepository.findByActivationKeyAndActive(request.activationKey, true)
            .orElseThrow { UserNotFoundException() }
        user.activationKey = null
        user.passwordDigest = securityUtil.getPasswordDigest(request.password)
        userRepository.save(user)
    }

    override fun resetPassword(request: ResetPasswordRequest) {
        val email = request.email
        val user = userRepository.findByLoginOrEmailAndActive(email, email, true)
            .orElseThrow { UserNotFoundException() }
            .apply { activationKey = securityUtil.newKey() }
            .let { userRepository.save(it) }

        val instanceUrl = securityUtil.getInstanceUrl(request.instanceKey, request.path)
        Events.passwordReset.onNext(PasswordReset(user, instanceUrl))
    }

    override fun getUserProfile(authToken: String): UserInfo = checkToken(authToken)
        .map { UserInfo(it, securityUtil.createToken(it)) }
        .getOrElse { throw UserNotFoundException() }

    fun checkToken(tokenKey: String): Option<User> {
        val token = tokenRepository.findById(tokenKey)
        return when {
            token.isPresent -> Option.empty()
            else -> securityUtil.fromToken(tokenKey).map { userRepository.getOne(it.id) }
        }
    }

    private fun preRegister(request: RegisterRequest): User {
        val instanceKey = checkNotNull(request.instanceKey) { "Instance key can not be null when activation" }
        val activationPath = checkNotNull(request.path) { "Activation path can not be null" }
        return preRegister(asUser(request), instanceKey, activationPath)
    }

    private fun preRegister(user: User, instanceKey: String, activationPath: String): User {
        val saved = userRepository.save(user.register(securityUtil.newKey()))
        userPreRegister.onNext(UserPreRegister(saved, securityUtil.getInstanceUrl(instanceKey, activationPath)))
        return saved
    }

    private fun register(request: RegisterRequest): User {
        val user = userRepository.save(asUser(request).activated())
        userRegister.onNext(UserRegister(user))
        return user
    }

    private fun asUser(registerRequest: RegisterRequest): User {
        return User(
            email = registerRequest.email,
            fullName = registerRequest.username,
            secret = securityUtil.newKey(),
            passwordDigest = securityUtil.getPasswordDigest(registerRequest.password))
    }
}
