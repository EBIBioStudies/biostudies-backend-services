package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.model.AccessPermission
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository

const val DEFAULT_USER = "default_user@ebi.ac.uk"

class UserPermissionsService(private val permissionRepository: AccessPermissionRepository) {
    fun allowedTags(user: String, accessType: AccessType): List<AccessPermission> =
        permissionRepository.findAllByUserEmailAndAccessType(user, accessType) +
        permissionRepository.findAllByUserEmailAndAccessType(DEFAULT_USER, accessType)

    fun hasPermission(user: String, accessTag: String, accessType: AccessType): Boolean =
        permissionRepository.existsByUserEmailAndAccessTypeAndAccessTagName(user, accessType, accessTag)
        .or(permissionRepository.existsByUserEmailAndAccessTypeAndAccessTagName(DEFAULT_USER, accessType, accessTag))
}
