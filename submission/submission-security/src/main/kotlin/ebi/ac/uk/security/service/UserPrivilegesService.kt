package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

internal class UserPrivilegesService(private val userRepository: UserDataRepository) : IUserPrivilegesService {
    override fun canProvideAccNo(email: String) =
        userRepository
            .findByEmailAndActive(email, true)
            .orElseThrow { UserNotFoundByEmailException(email) }
            .superuser

    // TODO: add proper security validation
    override fun canSubmit(accNo: String, email: String): Boolean {
        return true
    }

    // TODO: add proper security validation
    override fun canDelete(accNo: String, email: String): Boolean {
        return true
    }
}
