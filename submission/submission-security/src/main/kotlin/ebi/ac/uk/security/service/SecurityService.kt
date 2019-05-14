package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.security.integration.SecurityEvents
import ebi.ac.uk.security.integration.SecurityProperties
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.exception.ActKeyNotFoundException
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.integration.model.events.UserPreRegister
import ebi.ac.uk.security.integration.model.events.UserRegister
import ebi.ac.uk.security.util.SecurityUtil
import io.reactivex.subjects.Subject

internal class SecurityService(
    private val userRepository: UserDataRepository,
    private val securityUtil: SecurityUtil,
    private val securityProps: SecurityProperties,
    private val userPreRegister: Subject<UserPreRegister> = SecurityEvents.userPreRegister,
    private val userRegister: Subject<UserRegister> = SecurityEvents.userRegister
) : ISecurityService {

    override fun login(loginRequest: LoginRequest): UserInfo =
        userRepository
            .findByLoginOrEmailAndActive(loginRequest.login, loginRequest.login, true)
            .filter { securityUtil.checkPassword(it.passwordDigest, loginRequest.password) }
            .orElseThrow { LoginException() }
            .let { UserInfo(it, securityUtil.createToken(it)) }

    override fun registerUser(request: RegisterRequest): User {
        return when {
            userRepository.existsByEmail(request.email) -> throw UserAlreadyRegister(request.email)
            securityProps.requireActivation -> preRegister(request)
            else -> register(request)
        }
    }

    private fun preRegister(request: RegisterRequest): User {
        val instanceKey = checkNotNull(request.instanceKey) { "Instance key can not be null when activation" }
        val activationPath = checkNotNull(request.path) { "Activation path can not be null" }

        val user = userRepository.save(asUser(request).register(securityUtil.newKey()))
        userPreRegister.onNext(UserPreRegister(user, securityUtil.getInstanceUrl(instanceKey, activationPath)))
        return user
    }

    private fun register(request: RegisterRequest): User {
        val user = userRepository.save(asUser(request).activated())
        userRegister.onNext(UserRegister(user))
        return user
    }

    override fun activate(activationKey: String) {
        val user = userRepository.findByActivationKeyAndActive(activationKey, false)
            .orElseThrow { ActKeyNotFoundException() }

        user.activationKey = null
        user.active = true
        userRepository.save(user)
    }

    private fun asUser(registerRequest: RegisterRequest): User {
        return User(
            email = registerRequest.email,
            fullName = registerRequest.username,
            secret = securityUtil.newKey(),
            passwordDigest = securityUtil.getPasswordDigest(registerRequest.password))
    }
}
