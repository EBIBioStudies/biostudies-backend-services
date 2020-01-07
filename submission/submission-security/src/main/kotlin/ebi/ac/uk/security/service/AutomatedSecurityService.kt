package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.components.IAutomatedSecurityService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.util.SecurityUtil

internal class AutomatedSecurityService(
    private val profileService: ProfileService,
    private val securityUtil: SecurityUtil,
    private val userRepository: UserDataRepository
) : IAutomatedSecurityService {

    override fun getOrCreate(email: String, username: String): SecurityUser {
        return userRepository.findByEmail(email)
            .orElseGet { createUser(email, username) }
            .let { profileService.asSecurityUser(it) }
    }

    private fun createUser(email: String, username: String): User {
        val user = User(
            email = email,
            fullName = username,
            secret = securityUtil.newKey(),
            passwordDigest = ByteArray(0))
        user.active = false

        return userRepository.save(user)
    }
}
