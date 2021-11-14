package ebi.ac.uk.security.persistence

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserPendingRegistrationException
import ebi.ac.uk.security.integration.exception.UserWithActivationKeyNotFoundException


fun UserDataRepository.getByEmailOrThrowUserNotFound(email: String): DbUser {
    return findByEmail(email)
        ?: throw UserNotFoundByEmailException(email)
}

fun UserDataRepository.getUnActiveByEmail(email: String): DbUser {
    return findByEmailAndActive(email, false)
        ?: throw UserNotFoundByEmailException(email)
}

fun UserDataRepository.getUnActiveByActivationKey(key: String): DbUser {
    return findByActivationKeyAndActive(key, false)
        ?: throw UserWithActivationKeyNotFoundException()
}

fun UserDataRepository.getActiveByEmail(email: String): DbUser {
    return findByEmailAndActive(email, true)
        ?: throw UserNotFoundByEmailException(email)
}
fun UserDataRepository.getActiveByLoginOrEmailAndActive(login: String): DbUser {
    return findByLoginOrEmailAndActive(login,login, true)
        ?: throw UserNotFoundByEmailException(login)
}

fun UserDataRepository.getByActivationKey(key: String): DbUser {
    return findByActivationKey(key)
        ?: throw UserWithActivationKeyNotFoundException()
}

fun UserDataRepository.getUnActiveByEmailOrThrowUserPendingRegistration(email: String): DbUser {
    return findByEmailAndActive(email, false)
        ?: throw UserPendingRegistrationException(email)
}