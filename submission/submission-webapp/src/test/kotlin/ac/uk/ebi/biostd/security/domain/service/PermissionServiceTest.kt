package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PermissionServiceTest(
    @MockK private val permissionRepository: AccessPermissionRepository,
    @MockK private val userDataRepository: UserDataRepository,
    @MockK private val accessTagDataRepository: AccessTagDataRepo
) {
    private val testInstance = PermissionService(permissionRepository, userDataRepository, accessTagDataRepository)

    @Test
    fun `give permission to a user`() {
        every { userDataRepository.getByEmail(email) } returns dbUser
        every { accessTagDataRepository.findByName(accessTagName) } returns dbAccessTag
        every { permissionRepository.save(capture(dbAccessPermissionSlot)) } returns mockk()

        testInstance.givePermissionToUser(accessType, email, accessTagName)

        assertThat(dbAccessPermissionSlot.captured.user).isEqualTo(dbUser)
        assertThat(dbAccessPermissionSlot.captured.accessTag).isEqualTo(dbAccessTag)
    }

    companion object {
        const val email = "userEmail"
        const val accessTagName = "TAG_NAME"
        const val accessType = "READ"
        val dbUser = mockk<DbUser>()
        val dbAccessTag = mockk<DbAccessTag>()
        private val dbAccessPermissionSlot = slot<DbAccessPermission>()
    }
}
