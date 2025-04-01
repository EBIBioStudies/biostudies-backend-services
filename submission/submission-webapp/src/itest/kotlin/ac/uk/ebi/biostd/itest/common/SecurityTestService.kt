package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.service.SecurityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecurityTestService(
    private val securityService: SecurityService,
    private val securityQueryService: SecurityQueryService,
    private val userDataRepository: UserDataRepository,
    private val sequenceRepository: SequenceDataRepository,
) {
    fun ensureSequence(prefix: String) {
        if (sequenceRepository.existsByPrefix(prefix).not()) sequenceRepository.save(DbSequence(prefix))
    }

    fun getSecurityUser(email: String): SecurityUser = securityQueryService.getUser(email)

    suspend fun ensureUserRegistration(testUser: TestUser): Unit =
        withContext(Dispatchers.IO) {
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

    fun cleanUserFolder(simpleUser: TestUser) {
        val user = securityQueryService.getUser(simpleUser.email)
        FileUtils.cleanDirectory(user.userFolder.path.toFile())
    }
}
