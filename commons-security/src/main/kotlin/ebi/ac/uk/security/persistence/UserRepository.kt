package ebi.ac.uk.security.persistence

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.base.toOption
import java.security.MessageDigest
import java.util.UUID

class UserRepository(
        private val userDataRepository: UserDataRepository,
        private val sha1: MessageDigest = MessageDigest.getInstance("SHA1")
) {

    fun findByLoginOrEmail(loginEmail: String) =
            userDataRepository.findByLoginOrEmail(loginEmail, loginEmail).toOption()

    fun registerUser(email: String, login: String, password: String): User {
        val user = User(email, login, UUID.randomUUID().toString())
        user.passwordDigest = sha1.digest(password.toByteArray())
        return userDataRepository.save(user)
    }

    fun existByEmail(email: String) = userDataRepository.existsByEmail(email)
}
