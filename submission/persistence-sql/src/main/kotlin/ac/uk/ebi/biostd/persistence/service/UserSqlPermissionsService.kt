package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository

const val DEFAULT_USER = "default_user@ebi.ac.uk"

class UserSqlPermissionsService(private val permissionRepo: AccessPermissionRepository) {
    fun allowedTags(user: String, accessType: AccessType): List<DbAccessPermission> =
        permissionRepo.findAllByUserEmailAndAccessType(user, accessType) +
                permissionRepo.findAllByUserEmailAndAccessType(DEFAULT_USER, accessType)

    fun hasPermission(user: String, accessTag: String, accessType: AccessType): Boolean =
        permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(user, accessType, accessTag)
            .or(permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(DEFAULT_USER, accessType, accessTag))
}
