package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.extended.model.ExtUser
import ebi.ac.uk.security.integration.exception.UserNotFoundByIdException

class ExtUserService(private val userDataRepository: UserDataRepository) {
    fun getExtUser(id: Long): ExtUser =
        toExtUser(userDataRepository
            .findById(id)
            .orElseThrow { UserNotFoundByIdException(id) })

    private fun toExtUser(user: DbUser) = ExtUser(
        id = user.id,
        email = user.email,
        fullName = user.fullName,
        login = user.login,
        notificationsEnabled = user.notificationsEnabled)
}
