package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
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
    @MockK private val mockUser: User,
    @MockK private val mockUserRepository: UserDataRepository
) {
    private val testInstance = UserPrivilegesService(mockUserRepository)

    @BeforeEach
    fun beforeEach() {
        every { mockUser.superuser } returns true
        every { mockUserRepository.findByEmailAndActive("empty@mail.com", true) } returns Optional.empty()
        every { mockUserRepository.findByEmailAndActive("test@mail.com", true) } returns Optional.of(mockUser)
    }

    @Test
    fun `super user provides acc no`() {
        assertThat(testInstance.canProvideAccNo("test@mail.com")).isTrue()
    }

    @Test
    fun `regular user provides acc no`() {
        every { mockUser.superuser } returns false
        assertThat(testInstance.canProvideAccNo("test@mail.com")).isFalse()
    }

    @Test
    fun `non existing user`() {
        assertThrows<UserNotFoundByEmailException> { testInstance.canProvideAccNo("empty@mail.com") }
    }
}
