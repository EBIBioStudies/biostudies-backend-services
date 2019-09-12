package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

internal class UserPrivilegesService(
    private val userRepository: UserDataRepository,
    private val persistenceContext: PersistenceContext,
    private val accessPermissionRepository: AccessPermissionRepository
) : IUserPrivilegesService {
    override fun canProvideAccNo(email: String) = isSuperUser(email)

    override fun canSubmit(accNo: String, email: String): Boolean {
        val submission = persistenceContext.getSubmission(accNo)

        return persistenceContext.isNew(accNo) ||
            isSuperUser(email) ||
            isAuthor(submission!!, email).and(isNotInProject(submission)) ||
            hasTag(submission, AccessType.SUBMIT)
    }

    // TODO: add proper security validation
    override fun canDelete(accNo: String, email: String): Boolean {
        return true
    }

    private fun isNotInProject(submission: ExtendedSubmission) = submission.attachTo.isNullOrBlank()

    private fun hasTag(submission: ExtendedSubmission, accessType: AccessType) =
        accessPermissionRepository.existsByAccessTagInAndAccessType(submission.accessTags, accessType)

    private fun isSuperUser(email: String) = getUser(email).superuser

    private fun isAuthor(submission: ExtendedSubmission, email: String) = getUser(email).id == submission.user.id

    private fun getUser(email: String) =
        userRepository
            .findByEmailAndActive(email, true)
            .orElseThrow { UserNotFoundByEmailException(email) }
}
