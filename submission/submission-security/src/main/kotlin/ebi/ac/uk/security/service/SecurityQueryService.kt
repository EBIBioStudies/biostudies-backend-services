package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.MagicFolderType
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserNotFoundByTokenException
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.util.SecurityUtil

class SecurityQueryService(
    private val securityUtil: SecurityUtil,
    private val profileService: ProfileService,
    private val userRepository: UserDataRepository,
) : ISecurityQueryService {
    override fun existsByEmail(email: String, onlyActive: Boolean): Boolean {
        return if (onlyActive) userRepository.existsByEmailAndActive(email, true)
        else userRepository.existsByEmail(email)
    }

    override fun getUser(email: String): SecurityUser {
        return userRepository.findByEmail(email)
            ?.let { profileService.asSecurityUser(it) }
            ?: throw UserNotFoundByEmailException(email)
    }

    override fun getUserProfile(authToken: String): UserInfo {
        return securityUtil.checkToken(authToken)
            ?.let { profileService.getUserProfile(it, authToken) }
            ?: throw UserNotFoundByTokenException()
    }

    override fun getOrCreateInactive(email: String, username: String): SecurityUser {
        val user = userRepository.findByEmail(email) ?: createUserInactive(email, username)
        return profileService.asSecurityUser(user)
    }

    private fun createUserInactive(email: String, username: String): DbUser {
        val user = DbUser(
            email = email,
            fullName = username,
            secret = securityUtil.newKey(),
            magicFolderType = MagicFolderType.NFS,
            passwordDigest = ByteArray(0),
            notificationsEnabled = false
        )
        user.active = false
        user.activationKey = securityUtil.newKey()
        return userRepository.save(user)
    }
}
