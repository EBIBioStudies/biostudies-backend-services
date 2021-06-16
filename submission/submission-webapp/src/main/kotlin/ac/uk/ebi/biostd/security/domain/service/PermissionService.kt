package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import org.springframework.dao.EmptyResultDataAccessException

class PermissionService(
    private val permissionRepository: AccessPermissionRepository,
    private val userDataRepository: UserDataRepository,
    private val accessTagDataRepository: AccessTagDataRepo
) {
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
                accessType = AccessType.valueOf(accessType),
                user = user,
                accessTag = accessTag
            )
        )
    }
}
