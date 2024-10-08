package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.common.model.AccessType.DELETE
import ac.uk.ebi.biostd.persistence.common.model.AccessType.DELETE_FILES
import ac.uk.ebi.biostd.persistence.common.model.AccessType.UPDATE
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

@SuppressWarnings("TooManyFunctions")
internal class UserPrivilegesService(
    private val userRepository: UserDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val submissionQueryService: SubmissionMetaQueryService,
    private val permissionsService: UserPermissionsService,
) : IUserPrivilegesService {
    override suspend fun canProvideAccNo(
        submitter: String,
        collection: String,
    ): Boolean = isSuperUser(submitter) || isAdmin(submitter, collection)

    override fun canSubmitCollections(email: String) = isSuperUser(email)

    override fun canSubmitExtended(submitter: String): Boolean = isSuperUser(submitter)

    override suspend fun canSubmitToCollection(
        submitter: String,
        collection: String,
    ): Boolean =
        isSuperUser(submitter) ||
            isAdmin(submitter, collection) ||
            hasPermissions(submitter, collection, ATTACH)

    override fun allowedCollections(
        email: String,
        accessType: AccessType,
    ): List<String> =
        when {
            isSuperUser(email) -> tagsDataRepository.findAll().map { it.name }
            else ->
                permissionsService
                    .allowedTags(email, accessType)
                    .map { it.accessTag.name }
                    .distinct()
        }

    override suspend fun canResubmit(
        submitter: String,
        accNo: String,
    ): Boolean =
        isSuperUser(submitter) ||
            isAdmin(submitter, accNo) ||
            isAuthor(getOwner(accNo), submitter) ||
            hasPermissions(submitter, accNo, UPDATE)

    override suspend fun canDelete(
        submitter: String,
        accNo: String,
    ): Boolean =
        (isAuthor(getOwner(accNo), submitter) && isPublic(accNo).not()) ||
            hasPermissions(submitter, accNo, DELETE)

    override suspend fun canDeleteFiles(
        submitter: String,
        accNo: String,
    ): Boolean = hasPermissions(submitter, accNo, DELETE_FILES)

    override fun canRelease(email: String): Boolean = isSuperUser(email)

    override fun canUpdateReleaseDate(
        email: String,
        collection: String?,
    ): Boolean =
        when (collection) {
            null -> isSuperUser(email)
            else -> isSuperUser(email) || permissionsService.hasPermission(email, collection, ADMIN)
        }

    private suspend fun hasPermissions(
        user: String,
        accNo: String,
        accessType: AccessType,
    ): Boolean {
        val collections = submissionQueryService.getCollections(accNo)
        return permissionsService.hasPermission(user, accNo, accessType) ||
            (collections.isNotEmpty() && collections.all { permissionsService.hasPermission(user, it, accessType) })
    }

    private fun isSuperUser(email: String) = getUser(email).superuser

    private suspend fun isAdmin(
        email: String,
        accNo: String,
    ): Boolean {
        val collections = submissionQueryService.getCollections(accNo)
        return collections.isNotEmpty() && collections.all { permissionsService.isAdmin(email, it) }
    }

    private fun isAuthor(
        author: String?,
        email: String,
    ) = author == email

    private fun getUser(email: String) = userRepository.findByEmail(email) ?: throw UserNotFoundByEmailException(email)

    private suspend fun getOwner(accNo: String): String? = submissionQueryService.findLatestBasicByAccNo(accNo)?.owner

    private suspend fun isPublic(accNo: String): Boolean = submissionQueryService.findLatestBasicByAccNo(accNo)?.released.orFalse()
}
