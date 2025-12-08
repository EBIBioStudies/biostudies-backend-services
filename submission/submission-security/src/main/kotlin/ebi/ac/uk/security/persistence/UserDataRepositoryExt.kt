package ebi.ac.uk.security.persistence

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserWithActivationKeyNotFoundException

fun UserDataRepository.getInactiveByEmail(email: String): DbUser =
    findByEmailAndActive(email, false)
        ?: throw UserNotFoundByEmailException(email)

fun UserDataRepository.getInactiveByActivationKey(key: String): DbUser =
    findByActivationKeyAndActive(key, false)
        ?: throw UserWithActivationKeyNotFoundException()

fun UserDataRepository.getActiveByEmail(email: String): DbUser =
    findByEmailAndActive(email, true)
        ?: throw UserNotFoundByEmailException(email)

fun UserDataRepository.getByActivationKey(key: String): DbUser =
    findByActivationKey(key)
        ?: throw UserWithActivationKeyNotFoundException()
