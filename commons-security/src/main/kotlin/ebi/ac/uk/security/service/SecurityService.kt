package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.security.exception.UserNotFoundException
import ebi.ac.uk.security.model.UserInfo
import ebi.ac.uk.security.util.PasswordVerifier
import ebi.ac.uk.security.util.TokenUtil
import java.util.UUID

class SecurityService(
    private val userRepository: UserDataRepository,
    private val passwordVerifier: PasswordVerifier,
    private val tokenUtil: TokenUtil,
    private val requireActivation: Boolean
) {

    fun login(loginRequest: LoginRequest): UserInfo {
        val user = userRepository
                .findByLoginOrEmail(loginRequest.login, loginRequest.login)
                .orElseThrow { throw UserNotFoundException(loginRequest.login) }

        if (!passwordVerifier.checkPassword(user.passwordDigest, loginRequest.password)) {
            throw SecurityException("Given password do not match for user '${loginRequest.login}'")
        }

        return UserInfo(tokenUtil.createToken(user), user)
    }

    /**
     * Todo
     *  - add Aux profile information handling
     *
     */
    fun registerUser(registerRequest: RegisterRequest): User {
        if (userRepository.existsByEmail(registerRequest.email)) {
            throw SecurityException("There is already a user register with email ${registerRequest.email}")
        }

        val user = User(registerRequest.email, registerRequest.password, newRandomKey())
        user.email = registerRequest.email
        user.login = registerRequest.username
        user.passwordDigest = passwordVerifier.getPasswordDigest(registerRequest.password)

        return if (requireActivation) {
            userRepository.save(user.registered(registerRequest.activationUrl))
        } else {
            userRepository.save(user.activated())
        }
    }

    private fun newRandomKey() = UUID.randomUUID().toString()
}
