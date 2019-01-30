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
    private val tokenUtil: TokenUtil
) {

    fun login(loginRequest: LoginRequest): UserInfo {
        val user = userRepository.findByLoginOrEmail(loginRequest.login, loginRequest.login)
                .orElseThrow { throw UserNotFoundException(loginRequest.login) }

        if (!passwordVerifier.checkPassword(user.passwordDigest, loginRequest.password)) {
            throw SecurityException("Given password do not match for user '${loginRequest.login}'")
        }

        return UserInfo(tokenUtil.createToken(user), user)
    }

    /**
     * Todo
     *  - add logic for required activation
     *  - dispatch user register event and add :
     *      - add Notification handler to sent proper email
     *      - add notification handler to create magic folder and user symlink
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
        user.active = true
        return userRepository.save(user)
    }

    private fun newRandomKey() = UUID.randomUUID().toString()
}
