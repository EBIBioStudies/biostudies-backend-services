package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.itest.entities.DefaultUser
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestGroup
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.persistence.model.DbUserGroup
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.service.SecurityService

class SecurityTestService(
    private val securityService: SecurityService,
    private val userDataRepository: UserDataRepository,
    private val groupService: IGroupService,
) {

    fun registerUser(testUser: TestUser): SecurityUser {
        val user = securityService.registerUser(testUser.asRegisterRequest())
        if (testUser.superUser) {
            val dbUser = userDataRepository.getByEmail(user.email)
            dbUser.superuser = true
            userDataRepository.save(dbUser)
        }

        return user
    }

    fun createTestGroup(): DbUserGroup {
        return groupService.createGroup(TestGroup.testGroupName, TestGroup.testGroupDescription)
    }

    fun addUserInGroup(testUser: TestUser, group: DbUserGroup) {
        groupService.addUserInGroup(group.name, testUser.email)
    }

    fun deleteRegularUser() {
        userDataRepository.findByEmail(RegularUser.email)?.let { userDataRepository.delete(it) }
    }

    fun deleteSuperUser() {
        userDataRepository.findByEmail(SuperUser.email)?.let { userDataRepository.delete(it) }
    }

    fun deleteDefaultUser() {
        userDataRepository.findByEmail(DefaultUser.email)?.let { userDataRepository.delete(it) }
    }
}
