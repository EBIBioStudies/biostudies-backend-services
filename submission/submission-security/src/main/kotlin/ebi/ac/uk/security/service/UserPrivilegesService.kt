package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.common.model.AccessType.DELETE
import ac.uk.ebi.biostd.persistence.common.model.AccessType.UPDATE
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

@SuppressWarnings("TooManyFunctions")
internal class UserPrivilegesService(
    private val userRepository: UserDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val submissionQueryService: SubmissionMetaQueryService,
    private val userPermissionsService: UserPermissionsService
) : IUserPrivilegesService {
    override fun canProvideAccNo(email: String) = isSuperUser(email)

    override fun canSubmitProjects(email: String) = isSuperUser(email)

    override fun canSubmitExtended(submitter: String): Boolean = isSuperUser(submitter)

    override fun canSubmitToProject(submitter: String, project: String): Boolean =
        isSuperUser(submitter)
            .or(hasPermissions(submitter, listOf(project), ATTACH))
            .or(isAdmin(submitter, listOf(project), ATTACH))

    override fun allowedCollections(email: String, accessType: AccessType): List<String> = when {
        isSuperUser(email) -> tagsDataRepository.findAll().map { it.name }
        else ->
            userPermissionsService
                .allowedTags(email, accessType)
                .map { it.accessTag.name }
                .distinct()
    }

    override fun canResubmit(submitter: String, accNo: String): Boolean {
        val accessTags = submissionQueryService.getAccessTags(accNo)

        return isSuperUser(submitter)
            .or(isAuthor(getOwner(accNo), submitter))
            .or(hasPermissions(submitter, accessTags, UPDATE))
            .or(isAdmin(submitter, accessTags, UPDATE))
    }

    override fun canDelete(submitter: String, accNo: String): Boolean {
        val accessTags = submissionQueryService.getAccessTags(accNo)

        return isSuperUser(submitter)
            .or(isAuthor(getOwner(accNo), submitter))
            .or(hasPermissions(submitter, accessTags, DELETE))
            .or(isAdmin(submitter, accessTags, DELETE))
    }

    private fun hasPermissions(user: String, accessTags: List<String>, accessType: AccessType): Boolean {
        val tags = accessTags.filter { it != PUBLIC_ACCESS_TAG.value }

        return tags.isNotEmpty() && tags.all { userPermissionsService.hasPermission(user, it, accessType) }
    }

    private fun isAdmin(user: String, accessTags: List<String>, accessType: AccessType): Boolean {
        val tags = accessTags.filter { it != PUBLIC_ACCESS_TAG.value }

        return tags.isNotEmpty() && tags.all {
            userPermissionsService.hasPermission(user, it, ADMIN) or
                userPermissionsService.hasPermission(user, it, accessType)
        }
    }

    private fun isSuperUser(email: String) = getUser(email).superuser

    private fun isAuthor(author: String?, email: String) = author == email

    private fun getUser(email: String) =
        userRepository.findByEmail(email).orElseThrow { UserNotFoundByEmailException(email) }

    private fun getOwner(accNo: String) = submissionQueryService.findLatestBasicByAccNo(accNo)?.owner
}
