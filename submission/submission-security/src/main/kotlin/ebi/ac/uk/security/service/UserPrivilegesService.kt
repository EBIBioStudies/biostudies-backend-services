package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.model.AccessType.UPDATE
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.service.UserPermissionsService
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

@SuppressWarnings("TooManyFunctions")
internal class UserPrivilegesService(
    private val userRepository: UserDataRepository,
    private val tagsDataRepository: AccessTagDataRepo,
    private val submissionQueryService: SubmissionQueryService,
    private val userPermissionsService: UserPermissionsService
) : IUserPrivilegesService {
    override fun canProvideAccNo(email: String) = isSuperUser(email)

    override fun canSubmitProjects(email: String) = isSuperUser(email)

    override fun canSubmitExtended(submitter: String): Boolean = isSuperUser(submitter)

    override fun canSubmitToProject(submitter: String, project: String): Boolean =
        isSuperUser(submitter).or(hasPermissions(submitter, listOf(project), ATTACH))

    override fun allowedProjects(email: String, accessType: AccessType): List<String> = when {
        isSuperUser(email) -> tagsDataRepository.findAll().map { it.name }
        else ->
            userPermissionsService
                .allowedTags(email, accessType)
                .map { it.accessTag.name }
                .distinct()
    }

    override fun canResubmit(submitter: String, accNo: String) =
        isSuperUser(submitter)
            .or(isAuthor(submissionQueryService.getAuthor(accNo), submitter))
            .or(hasPermissions(submitter, submissionQueryService.getAccessTags(accNo), UPDATE))

    override fun canDelete(submitter: String, accNo: String) =
        isSuperUser(submitter)
            .or(isAuthor(submissionQueryService.getAuthor(accNo), submitter))
            .or(hasPermissions(submitter, submissionQueryService.getAccessTags(accNo), AccessType.DELETE))

    private fun hasPermissions(user: String, accessTags: List<String>, accessType: AccessType): Boolean {
        val tags = accessTags.filter { it != PUBLIC_ACCESS_TAG.value }

        return tags.isNotEmpty() && tags.all { userPermissionsService.hasPermission(user, it, accessType) }
    }

    private fun isSuperUser(email: String) = getUser(email).superuser

    private fun isAuthor(author: String, email: String) = author == email

    private fun getUser(email: String) =
        userRepository.findByEmail(email).orElseThrow { UserNotFoundByEmailException(email) }
}
