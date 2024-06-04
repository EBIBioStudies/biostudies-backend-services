package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.AccessType.READ
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.security.domain.exception.PermissionsUserDoesNotExistsException
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PermissionServiceTest(
    @MockK private val subQueryService: SubmissionMetaQueryService,
    @MockK private val permissionRepo: AccessPermissionRepository,
    @MockK private val userRepository: UserDataRepository,
    @MockK private val tagRepository: AccessTagDataRepo,
    @MockK private val dbAccessTag: DbAccessTag,
    @MockK private val dbUser: DbUser,
) {
    private val testInstance = PermissionService(subQueryService, permissionRepo, userRepository, tagRepository)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        coEvery { subQueryService.existByAccNo(ACC_NO) } returns true
        every { userRepository.findByEmail(EMAIL) } returns dbUser
        every { tagRepository.findByName(ACC_NO) } returns dbAccessTag
        every { permissionRepo.save(capture(dbAccessPermissionSlot)) } returns mockk()
        every { permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(EMAIL, READ, ACC_NO) } returns false
    }

    @Test
    fun `grant permission`() =
        runTest {
            testInstance.grantPermission(READ, EMAIL, ACC_NO)

            assertThat(dbAccessPermissionSlot.captured.user).isEqualTo(dbUser)
            assertThat(dbAccessPermissionSlot.captured.accessTag).isEqualTo(dbAccessTag)
        }

    @Test
    fun `when the permission already exists`() =
        runTest {
            every { permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(EMAIL, READ, ACC_NO) } returns true

            testInstance.grantPermission(READ, EMAIL, ACC_NO)

            verify(exactly = 0) { permissionRepo.save(any()) }
        }

    @Test
    fun `when not existing tag`() =
        runTest {
            val accessTagSlot = slot<DbAccessTag>()

            every { tagRepository.findByName(ACC_NO) } returns null
            every { tagRepository.save(capture(accessTagSlot)) } returns dbAccessTag

            testInstance.grantPermission(READ, EMAIL, ACC_NO)

            verify(exactly = 1) { tagRepository.save(accessTagSlot.captured) }
            assertThat(dbAccessPermissionSlot.captured.user).isEqualTo(dbUser)
            assertThat(dbAccessPermissionSlot.captured.accessTag).isEqualTo(dbAccessTag)
        }

    @Test
    fun `when not existing user`() =
        runTest {
            every { userRepository.findByEmail(EMAIL) } returns null

            val error =
                assertThrows<PermissionsUserDoesNotExistsException> {
                    testInstance.grantPermission(READ, EMAIL, ACC_NO)
                }

            assertThat(error.message).isEqualTo("The user $EMAIL does not exist")
        }

    @Test
    fun `when not existing submission`() =
        runTest {
            coEvery { subQueryService.existByAccNo(ACC_NO) } returns false

            val error =
                assertThrows<SubmissionNotFoundException> {
                    testInstance.grantPermission(READ, EMAIL, ACC_NO)
                }

            assertThat(error.message).isEqualTo("The submission '$ACC_NO' was not found")
        }

    private companion object {
        const val EMAIL = "email@test.org"
        const val ACC_NO = "ArrayExpress"
        val dbAccessPermissionSlot = slot<DbAccessPermission>()
    }
}
