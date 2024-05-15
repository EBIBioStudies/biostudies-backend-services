package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.security.domain.exception.PermissionsAccessTagDoesNotExistsException
import ac.uk.ebi.biostd.security.domain.exception.PermissionsUserDoesNotExistsException

class PermissionService(
    private val permissionRepository: AccessPermissionRepository,
    private val userRepository: UserDataRepository,
    private val tagRepository: AccessTagDataRepo,
) {
    fun grantPermission(
        accessType: AccessType,
        email: String,
        tag: String,
    ) {
        val user = userRepository.findByEmail(email) ?: throw PermissionsUserDoesNotExistsException(email)
        val accessTag = tagRepository.findByName(tag) ?: throw PermissionsAccessTagDoesNotExistsException(tag)

        if (permissionExists(accessType, email, tag).not()) {
            permissionRepository.save(DbAccessPermission(accessType = accessType, user = user, accessTag = accessTag))
        }
    }

    fun createAndGrantPermission(
        accessType: AccessType,
        email: String,
        tag: String,
    ) {
        if (tagRepository.existsByName(tag).not()) tagRepository.save(DbAccessTag(name = tag))
        grantPermission(accessType, email, tag)
    }

    private fun permissionExists(
        accessType: AccessType,
        email: String,
        accessTagName: String,
    ): Boolean =
        permissionRepository.existsByUserEmailAndAccessTypeAndAccessTagName(
            email,
            accessType,
            accessTagName,
        )
}
