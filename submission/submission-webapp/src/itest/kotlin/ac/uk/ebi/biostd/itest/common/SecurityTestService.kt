package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.service.SecurityService

class SecurityTestService(
    private val securityService: SecurityService,
    private val userDataRepository: UserDataRepository,
) {
    private fun registerUser(testUser: TestUser): SecurityUser {
        val user = securityService.registerUser(testUser.asRegisterRequest())
        if (testUser.superUser) {
            val dbUser = userDataRepository.getByEmail(user.email)
            dbUser.superuser = true
            userDataRepository.save(dbUser)
        }

        return user
    }

    fun ensureUserRegistration(testUser: TestUser) {
        if (userDataRepository.existsByEmail(testUser.email).not()) registerUser(testUser)
    }
}
