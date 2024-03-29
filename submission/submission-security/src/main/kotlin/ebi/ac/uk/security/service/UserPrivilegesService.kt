package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.common.model.AccessType.DELETE
import ac.uk.ebi.biostd.persistence.common.model.AccessType.UPDATE
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

@SuppressWarnings("TooManyFunctions")
internal class UserPrivilegesService(
    private val userRepository: UserDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val submissionQueryService: SubmissionMetaQueryService,
    private val userPermissionsService: UserPermissionsService,
) : IUserPrivilegesService {
    override fun canProvideAccNo(email: String) = isSuperUser(email)

    override fun canSubmitCollections(email: String) = isSuperUser(email)

    override fun canSubmitExtended(submitter: String): Boolean = isSuperUser(submitter)

    override fun canSubmitToCollection(submitter: String, collection: String): Boolean {
        val accessTags = listOf(collection)

        return isSuperUser(submitter) ||
            isAdmin(submitter, accessTags) ||
            hasPermissions(submitter, accessTags, ATTACH)
    }

    override fun allowedCollections(email: String, accessType: AccessType): List<String> = when {
        isSuperUser(email) -> tagsDataRepository.findAll().map { it.name }
        else ->
            userPermissionsService
                .allowedTags(email, accessType)
                .map { it.accessTag.name }
                .distinct()
    }

    override suspend fun canResubmit(submitter: String, accNo: String): Boolean {
        val accessTags = submissionQueryService.getAccessTags(accNo)

        return isSuperUser(submitter) ||
            isAdmin(submitter, accessTags) ||
            isAuthor(getOwner(accNo), submitter) ||
            hasPermissions(submitter, accessTags, UPDATE)
    }

    override suspend fun canDelete(submitter: String, accNo: String): Boolean {
        return (isAuthor(getOwner(accNo), submitter) && isPublic(accNo).not()) ||
            hasPermissions(submitter, submissionQueryService.getAccessTags(accNo), DELETE)
    }

    override fun canRelease(email: String): Boolean = isSuperUser(email)

    override fun canSuppress(email: String): Boolean = isSuperUser(email)

    private fun hasPermissions(user: String, accessTags: List<String>, accessType: AccessType): Boolean {
        val tags = accessTags.filter { it != PUBLIC_ACCESS_TAG.value }
        return tags.isNotEmpty() && tags.all { userPermissionsService.hasPermission(user, it, accessType) }
    }

    private fun isSuperUser(email: String) = getUser(email).superuser

    private fun isAdmin(email: String, accessTags: List<String>): Boolean {
        return accessTags.isNotEmpty() && accessTags.all { userPermissionsService.isAdmin(email, it) }
    }

    private fun isAuthor(author: String?, email: String) = author == email

    private fun getUser(email: String) = userRepository.findByEmail(email) ?: throw UserNotFoundByEmailException(email)

    private suspend fun getOwner(accNo: String) = submissionQueryService.findLatestBasicByAccNo(accNo)?.owner

    private suspend fun isPublic(accNo: String) =
        submissionQueryService.findLatestBasicByAccNo(accNo)?.released.orFalse()
}
