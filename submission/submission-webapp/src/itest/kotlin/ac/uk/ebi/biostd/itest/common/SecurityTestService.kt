package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.service.SecurityService

class SecurityTestService(
    private val securityService: SecurityService,
    private val userDataRepository: UserDataRepository,
    private val sequenceRepository: SequenceDataRepository,
) {
    fun ensureSequence(prefix: String) {
        if (sequenceRepository.existsByPrefix(prefix).not()) sequenceRepository.save(DbSequence(prefix))
    }

    suspend fun ensureUserRegistration(testUser: TestUser) {
        if (userDataRepository.existsByEmail(testUser.email).not()) registerUser(testUser)
    }

    private suspend fun registerUser(testUser: TestUser): SecurityUser {
        val user = securityService.registerUser(testUser.asRegisterRequest())
        if (testUser.superUser) {
            val dbUser = userDataRepository.getByEmail(user.email)
            dbUser.superuser = true
            userDataRepository.save(dbUser)
        }

        return user
    }
}
