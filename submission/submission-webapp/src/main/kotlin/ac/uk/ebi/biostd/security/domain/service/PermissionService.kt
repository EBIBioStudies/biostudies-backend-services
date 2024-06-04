package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.security.domain.exception.PermissionsUserDoesNotExistsException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PermissionService(
    private val submissionQueryService: SubmissionMetaQueryService,
    private val permissionRepository: AccessPermissionRepository,
    private val userRepository: UserDataRepository,
    private val tagRepository: AccessTagDataRepo,
) {
    suspend fun grantPermission(
        accessType: AccessType,
        email: String,
        accNo: String,
    ) = withContext(Dispatchers.IO) {
        require(submissionQueryService.existByAccNo(accNo)) { throw SubmissionNotFoundException(accNo) }

        val user = userRepository.findByEmail(email) ?: throw PermissionsUserDoesNotExistsException(email)
        val accessTag = tagRepository.findByName(accNo) ?: tagRepository.save(DbAccessTag(name = accNo))

        if (permissionExists(accessType, email, accNo).not()) {
            permissionRepository.save(DbAccessPermission(accessType = accessType, user = user, accessTag = accessTag))
        }
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
