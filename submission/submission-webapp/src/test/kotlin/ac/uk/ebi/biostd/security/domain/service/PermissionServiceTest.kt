package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType.READ
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.security.domain.exception.PermissionsAccessTagDoesNotExistsException
import ac.uk.ebi.biostd.security.domain.exception.PermissionsUserDoesNotExistsException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PermissionServiceTest(
    @MockK private val permissionRepo: AccessPermissionRepository,
    @MockK private val userRepository: UserDataRepository,
    @MockK private val tagRepository: AccessTagDataRepo,
) {
    private val testInstance = PermissionService(permissionRepo, userRepository, tagRepository)

    @Test
    fun `give permission to a user`() {
        every { userRepository.findByEmail(EMAIL) } returns dbUser
        every { tagRepository.findByName(ACCESS_TAG) } returns dbAccessTag
        every { permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(EMAIL, READ, ACCESS_TAG) } returns false
        every { permissionRepo.save(capture(dbAccessPermissionSlot)) } returns mockk()

        testInstance.givePermissionToUser(accessType, EMAIL, ACCESS_TAG)

        assertThat(dbAccessPermissionSlot.captured.user).isEqualTo(dbUser)
        assertThat(dbAccessPermissionSlot.captured.accessTag).isEqualTo(dbAccessTag)
    }

    @Test
    fun `give permission to a user when already exists`() {
        every { userRepository.findByEmail(EMAIL) } returns dbUser
        every { tagRepository.findByName(ACCESS_TAG) } returns dbAccessTag
        every { permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(EMAIL, READ, ACCESS_TAG) } returns true

        testInstance.givePermissionToUser(accessType, EMAIL, ACCESS_TAG)
    }

    @Test
    fun `when not existing user`() {
        every { userRepository.findByEmail(EMAIL) } returns null

        assertThrows<PermissionsUserDoesNotExistsException> {
            testInstance.givePermissionToUser(accessType, EMAIL, ACCESS_TAG)
        }
    }

    @Test
    fun `when not existing tag`() {
        every { userRepository.findByEmail(EMAIL) } returns dbUser
        every { tagRepository.findByName(ACCESS_TAG) } returns null

        assertThrows<PermissionsAccessTagDoesNotExistsException> {
            testInstance.givePermissionToUser(accessType, EMAIL, ACCESS_TAG)
        }
    }

    companion object {
        const val EMAIL = "userEmail"
        const val ACCESS_TAG = "TAG_NAME"
        val accessType = READ
        val dbUser = mockk<DbUser>()
        val dbAccessTag = mockk<DbAccessTag>()
        private val dbAccessPermissionSlot = slot<DbAccessPermission>()
    }
}
