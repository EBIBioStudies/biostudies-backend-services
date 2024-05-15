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
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { userRepository.findByEmail(EMAIL) } returns dbUser
        every { tagRepository.findByName(ACCESS_TAG) } returns dbAccessTag
        every { permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(EMAIL, READ, ACCESS_TAG) } returns false
        every { permissionRepo.save(capture(dbAccessPermissionSlot)) } returns mockk()
    }

    @Test
    fun `grant permission`() {
        testInstance.grantPermission(accessType, EMAIL, ACCESS_TAG)

        assertThat(dbAccessPermissionSlot.captured.user).isEqualTo(dbUser)
        assertThat(dbAccessPermissionSlot.captured.accessTag).isEqualTo(dbAccessTag)
    }

    @Test
    fun `grant permission when already exists`() {
        every { permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(EMAIL, READ, ACCESS_TAG) } returns true

        testInstance.grantPermission(accessType, EMAIL, ACCESS_TAG)

        verify(exactly = 0) { permissionRepo.save(any()) }
    }

    @Test
    fun `create and grant permission`() {
        val accessTagSlot = slot<DbAccessTag>()

        every { tagRepository.existsByName(ACCESS_TAG) } returns false
        every { tagRepository.save(capture(accessTagSlot)) } returns dbAccessTag

        testInstance.createAndGrantPermission(accessType, EMAIL, ACCESS_TAG)

        verify(exactly = 1) { tagRepository.save(accessTagSlot.captured) }
        assertThat(dbAccessPermissionSlot.captured.user).isEqualTo(dbUser)
        assertThat(dbAccessPermissionSlot.captured.accessTag).isEqualTo(dbAccessTag)
    }

    @Test
    fun `create and grant permission when already exists`() {
        every { tagRepository.existsByName(ACCESS_TAG) } returns true

        testInstance.createAndGrantPermission(accessType, EMAIL, ACCESS_TAG)

        verify(exactly = 0) { tagRepository.save(any()) }
        assertThat(dbAccessPermissionSlot.captured.user).isEqualTo(dbUser)
        assertThat(dbAccessPermissionSlot.captured.accessTag).isEqualTo(dbAccessTag)
    }

    @Test
    fun `grant permission when not existing user`() {
        every { userRepository.findByEmail(EMAIL) } returns null

        assertThrows<PermissionsUserDoesNotExistsException> {
            testInstance.grantPermission(accessType, EMAIL, ACCESS_TAG)
        }
    }

    @Test
    fun `grant permission when not existing tag`() {
        every { userRepository.findByEmail(EMAIL) } returns dbUser
        every { tagRepository.findByName(ACCESS_TAG) } returns null

        assertThrows<PermissionsAccessTagDoesNotExistsException> {
            testInstance.grantPermission(accessType, EMAIL, ACCESS_TAG)
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
