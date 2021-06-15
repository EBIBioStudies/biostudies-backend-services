package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import org.springframework.dao.EmptyResultDataAccessException

const val DEFAULT_USER = "default_user@ebi.ac.uk"

class UserSqlPermissionsService(
    private val permissionRepository: AccessPermissionRepository,
    private val userDataRepository: UserDataRepository,
    private val accessTagDataRepository: AccessTagDataRepo
) {
    fun allowedTags(user: String, accessType: AccessType): List<DbAccessPermission> =
        permissionRepository.findAllByUserEmailAndAccessType(user, accessType) +
            permissionRepository.findAllByUserEmailAndAccessType(DEFAULT_USER, accessType)

    fun hasPermission(user: String, accessTag: String, accessType: AccessType): Boolean =
        permissionRepository.existsByUserEmailAndAccessTypeAndAccessTagName(user, accessType, accessTag)
            .or(
                permissionRepository.existsByUserEmailAndAccessTypeAndAccessTagName(
                    DEFAULT_USER,
                    accessType,
                    accessTag
                )
            )

    fun givePermissionToUser(accessType: String, email: String, accessTagName: String) {
        val user = userDataRepository.getByEmail(email)
        val accessTag = try {
            accessTagDataRepository.findByName(accessTagName)
        } catch (exception: EmptyResultDataAccessException) {
            print(exception)
            accessTagDataRepository.save(DbAccessTag(name = accessTagName))
        }

        permissionRepository.save(
            DbAccessPermission(
                accessType = AccessType.valueOf(accessType.trim().toUpperCase()),
                user = user,
                accessTag = accessTag
            )
        )
    }
}
