package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import ac.uk.ebi.biostd.persistence.model.User as UserDB

@ExtendWith(MockKExtension::class)
@Disabled
class UserPrivilegesServiceTest(
    @MockK private val author: UserDB,
    @MockK private val otherAuthor: UserDB,
    @MockK private val superuser: UserDB,
    @MockK private val userRepository: UserDataRepository,
    @MockK private val accessPermissionRepository: AccessPermissionRepository
) {
    private val testAuthor = "author@mail.com"
    private val testOtherAuthor = "otherAuthor@mail.com"
    private val testInstance = UserPrivilegesService(userRepository, accessPermissionRepository)

    @BeforeEach
    fun beforeEach() {
        initUsers()
        initAccessPermissions()
    }

    @Test
    fun `super user provides acc no`() {
        assertThat(testInstance.canProvideAccNo("superuser@mail.com")).isTrue()
    }

    @Test
    fun `regular user provides acc no`() {
        every { superuser.superuser } returns false
        assertThat(testInstance.canProvideAccNo("superuser@mail.com")).isFalse()
    }

    @Test
    fun `resubmit as super user`() {
        assertThat(testInstance.canResubmit("superuser@mail.com", testAuthor, emptyList())).isTrue()
    }

    @Test
    fun `author user with tag resubmits a submission that is in a project`() {
        assertThat(testInstance.canResubmit("author@mail.com", testAuthor, emptyList())).isTrue()
    }

    @Test
    fun `super user deletes a submission`() {
        assertThat(testInstance.canDelete("superuser@mail.com", testAuthor, emptyList())).isTrue()
    }

    @Test
    fun `author user deletes own submission`() {
        assertThat(testInstance.canDelete("author@mail.com", testAuthor, emptyList())).isTrue()
    }

    @Test
    fun `author user deletes not own submission`() {
        assertThat(testInstance.canDelete("author@mail.com", testOtherAuthor, emptyList())).isFalse()
    }

    @Test
    fun `other author user deletes submission with tag`() {
        assertThat(testInstance.canDelete("otherAuthor@mail.com", testAuthor, listOf("A-Project"))).isTrue()
    }

    @Test
    fun `other author user deletes submission without tag`() {
        assertThat(testInstance.canDelete("otherAuthor@mail.com", testAuthor, emptyList())).isFalse()
    }

    @Test
    fun `non existing user`() {
        assertThrows<UserNotFoundByEmailException> { testInstance.canProvideAccNo("empty@mail.com") }
    }

    private fun initUsers() {
        every { superuser.id } returns 123
        every { superuser.superuser } returns true

        every { author.id } returns 124
        every { author.superuser } returns false

        every { otherAuthor.id } returns 125
        every { otherAuthor.superuser } returns false

        every { userRepository.findByEmail("empty@mail.com") } returns Optional.empty()
        every { userRepository.findByEmail("author@mail.com") } returns Optional.of(author)
        every { userRepository.findByEmail("otherAuthor@mail.com") } returns Optional.of(otherAuthor)
        every { userRepository.findByEmail("superuser@mail.com") } returns Optional.of(superuser)
    }

    private fun initAccessPermissions() {
        every {
            accessPermissionRepository.existsByAccessTagNameInAndAccessType(listOf("A-Project"), AccessType.SUBMIT)
        } returns true

        every {
            accessPermissionRepository.existsByAccessTagNameInAndAccessType(emptyList(), AccessType.SUBMIT)
        } returns false

        every {
            accessPermissionRepository.existsByAccessTagNameInAndAccessType(emptyList(), AccessType.DELETE)
        } returns false

        every {
            accessPermissionRepository.existsByAccessTagNameInAndAccessType(listOf("A-Project"), AccessType.DELETE)
        } returns true
    }
}
