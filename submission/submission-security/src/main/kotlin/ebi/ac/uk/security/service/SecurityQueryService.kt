package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import arrow.core.getOrElse
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.exception.UserNotFoundByTokenException
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.util.SecurityUtil

class SecurityQueryService(
    private val securityUtil: SecurityUtil,
    private val profileService: ProfileService,
    private val userRepository: UserDataRepository
) : ISecurityQueryService {
    override fun existsByEmail(email: String): Boolean = userRepository.existsByEmailAndActive(email, active = true)

    override fun getUser(email: String): SecurityUser =
        userRepository.findByEmailAndActive(email, true)
            .map { profileService.asSecurityUser(it) }
            .orElseThrow { throw UserAlreadyRegister(email) }

    override fun getUserProfile(authToken: String): UserInfo =
        securityUtil.checkToken(authToken)
            .getOrElse { throw UserNotFoundByTokenException() }
            .let { profileService.getUserProfile(it, authToken) }

    override fun getOrCreateInactive(email: String, username: String): SecurityUser {
        val dbUser = userRepository.findByEmail(email) ?: createUserInactive(email, username)
        return dbUser.let { profileService.asSecurityUser(it) }
    }

    private fun createUserInactive(email: String, username: String): DbUser {
        val user = DbUser(
            email = email,
            fullName = username,
            secret = securityUtil.newKey(),
            passwordDigest = ByteArray(0),
            notificationsEnabled = false
        ).apply {
            active = false
            activationKey = securityUtil.newKey()
        }

        return userRepository.save(user)
    }
}
