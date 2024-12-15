package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.model.FolderStats
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
    private val securityProperties: SecurityProperties,
) : ISecurityQueryService {
    override fun existsByEmail(
        email: String,
        onlyActive: Boolean,
    ): Boolean =
        if (onlyActive) {
            userRepository.existsByEmailAndActive(email, true)
        } else {
            userRepository.existsByEmail(email)
        }

    override fun getUser(email: String): SecurityUser =
        userRepository
            .findByEmail(email)
            ?.let { profileService.asSecurityUser(it) }
            ?: throw UserNotFoundByEmailException(email)

    override fun getUserProfile(authToken: String): UserInfo =
        securityUtil
            .checkToken(authToken)
            ?.let { profileService.getUserProfile(it, authToken) }
            ?: throw UserNotFoundByTokenException()

    override fun getUserFolderStats(email: String): FolderStats {
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundByEmailException(email)
        return profileService.getUserFolderStats(user)
    }

    override fun getOrCreateInactive(
        email: String,
        username: String,
    ): SecurityUser {
        val user = userRepository.findByEmail(email) ?: createUserInactive(email, username)
        return profileService.asSecurityUser(user)
    }

    private fun createUserInactive(
        email: String,
        username: String,
    ): DbUser {
        val user =
            DbUser(
                email = email.lowercase(),
                fullName = username,
                secret = securityUtil.newKey(),
                storageMode = securityProperties.filesProperties.defaultMode,
                passwordDigest = ByteArray(0),
                notificationsEnabled = false,
            )
        user.active = false
        user.activationKey = securityUtil.newKey()
        return userRepository.save(user)
    }
}
