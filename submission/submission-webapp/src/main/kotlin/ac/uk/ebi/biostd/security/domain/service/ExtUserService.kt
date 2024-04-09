package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.extended.model.ExtUser
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

class ExtUserService(private val userDataRepository: UserDataRepository) {
    fun getExtUser(email: String): ExtUser = toExtUser(userDataRepository.findByEmail(email) ?: throw UserNotFoundByEmailException(email))

    private fun toExtUser(user: DbUser) =
        ExtUser(
            email = user.email,
            fullName = user.fullName,
            login = user.login,
            notificationsEnabled = user.notificationsEnabled,
        )
}
