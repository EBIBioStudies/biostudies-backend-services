package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.model.User as UserDB
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.model.User
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
    @MockK private val author: UserDB,
    @MockK private val superuser: UserDB,
    @MockK private val userRepository: UserDataRepository,
    @MockK private val accessPermissionRepository: AccessPermissionRepository
) {
    private val testAuthor = User(124, "author@mail.com", "a-secret")
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
        assertThat(testInstance.canResubmit("superuser@mail.com", testAuthor, null, emptyList())).isTrue()
    }

    @Test
    fun `author user resubmits a submission that is not in a project`() {
        assertThat(testInstance.canResubmit("author@mail.com", testAuthor, null, emptyList())).isTrue()
    }

    @Test
    fun `author user resubmits a submission that is in a project`() {
        assertThat(testInstance.canResubmit("author@mail.com", testAuthor, "A-Project", emptyList())).isFalse()
    }

    @Test
    fun `author user with tag resubmits a submission that is in a project`() {
        assertThat(testInstance.canResubmit("author@mail.com", testAuthor, "A-Project", listOf("A-Project"))).isTrue()
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

        every { userRepository.findByEmailAndActive("empty@mail.com", true) } returns Optional.empty()
        every { userRepository.findByEmailAndActive("author@mail.com", true) } returns Optional.of(author)
        every { userRepository.findByEmailAndActive("superuser@mail.com", true) } returns Optional.of(superuser)
    }

    private fun initAccessPermissions() {
        every {
            accessPermissionRepository.existsByAccessTagInAndAccessType(listOf("A-Project"), AccessType.SUBMIT)
        } returns true

        every {
            accessPermissionRepository.existsByAccessTagInAndAccessType(emptyList(), AccessType.SUBMIT)
        } returns false
    }
}
