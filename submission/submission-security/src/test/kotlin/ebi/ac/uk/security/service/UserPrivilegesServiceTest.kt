package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.model.User as UserDB
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
class UserPrivilegesServiceTest(
    @MockK private val user: UserDB,
    @MockK private val userRepository: UserDataRepository,
    @MockK private val existingSubmission: ExtendedSubmission,
    @MockK private val persistenceContext: PersistenceContext,
    @MockK private val accessPermissionRepository: AccessPermissionRepository
) {
    private val testInstance = UserPrivilegesService(userRepository, persistenceContext, accessPermissionRepository)

    @BeforeEach
    fun beforeEach() {
        initUser()
        initPersistenceContext()
        initExistingSubmission()
    }

    @Test
    fun `super user provides acc no`() {
        assertThat(testInstance.canProvideAccNo("test@mail.com")).isTrue()
    }

    @Test
    fun `regular user provides acc no`() {
        every { user.superuser } returns false
        assertThat(testInstance.canProvideAccNo("test@mail.com")).isFalse()
    }

    @Test
    fun `submit new submission`() {
        assertThat(testInstance.canSubmit("S-NEW123", "test@mail.com")).isTrue()
    }

    @Test
    fun `submit existing submission with super user`() {
        assertThat(testInstance.canSubmit("S-OLD123", "test@mail.com")).isTrue()
    }

    @Test
    fun `regular author user resubmits existing submission that is not in a project`() {
        every { user.superuser } returns false
        assertThat(testInstance.canSubmit("S-OLD123", "test@mail.com")).isTrue()
    }

    @Test
    fun `regular author user resubmits existing submission that is in a project`() {
        every { user.superuser } returns false
        every { existingSubmission.attachTo } returns "A Project"

        assertThat(testInstance.canSubmit("S-OLD123", "test@mail.com")).isFalse()
    }

    @Test
    fun `regular author user with tag resubmits existing submission that is in a project`() {
        every { user.superuser } returns false
        every { existingSubmission.attachTo } returns "A Project"
        every {
            accessPermissionRepository.existsByAccessTagInAndAccessType(emptyList(), AccessType.SUBMIT)
        } returns true

        assertThat(testInstance.canSubmit("S-OLD123", "test@mail.com")).isTrue()
    }

    @Test
    fun `non existing user`() {
        assertThrows<UserNotFoundByEmailException> { testInstance.canProvideAccNo("empty@mail.com") }
    }

    private fun initUser() {
        every { user.id } returns 123
        every { user.superuser } returns true
        every { userRepository.findByEmailAndActive("empty@mail.com", true) } returns Optional.empty()
        every { userRepository.findByEmailAndActive("test@mail.com", true) } returns Optional.of(user)
    }

    private fun initPersistenceContext() {
        every { persistenceContext.isNew("S-NEW123") } returns true
        every { persistenceContext.isNew("S-OLD123") } returns false
        every { persistenceContext.getSubmission("S-NEW123") } returns null
        every { persistenceContext.getSubmission("S-OLD123") } returns existingSubmission
    }

    private fun initExistingSubmission() {
        every { existingSubmission.user } returns User(123, "test@mail.com", "a-secret")
        every { existingSubmission.attachTo } returns null
        every { existingSubmission.accessTags } returns mutableListOf()
        every {
            accessPermissionRepository.existsByAccessTagInAndAccessType(emptyList(), AccessType.SUBMIT)
        } returns false
    }
}
