package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.itest.entities.TestGroup
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.persistence.model.UserGroup
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.service.SecurityService

class SecurityTestService(
    private val securityService: SecurityService,
    private val userDataRepository: UserDataRepository,
    private val groupService: IGroupService
) {

    fun registerUser(testUser: TestUser): SecurityUser {
        val user = securityService.registerUser(testUser.asRegisterRequest())
        if (testUser.superUser) {
            val dbUser = userDataRepository.getByEmail(user.email)
            dbUser.superuser = true
            userDataRepository.save(dbUser)
        }

        return user;
    }

    fun createTestGroup(): UserGroup {
        return groupService.createGroup(TestGroup.testGroupName, TestGroup.testGroupDescription)
    }

    fun addUserInGroup(testUser: TestUser, group: UserGroup) {
        groupService.addUserInGroup(group.name, testUser.email)
    }
}
