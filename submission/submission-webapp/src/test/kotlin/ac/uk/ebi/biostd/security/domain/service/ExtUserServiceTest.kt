package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ExtUserServiceTest(@MockK private val userDataRepository: UserDataRepository) {
    private val testInstance = ExtUserService(userDataRepository)

    @Test
    fun getExtUser() {
        val user = mockUser()
        every { userDataRepository.findByEmail("test@ebi.ac.uk") } returns user

        val extUser = testInstance.getExtUser("test@ebi.ac.uk")
        assertThat(extUser.login).isEqualTo("test")
        assertThat(extUser.fullName).isEqualTo("Test User")
        assertThat(extUser.email).isEqualTo("test@ebi.ac.uk")
        assertThat(extUser.notificationsEnabled).isTrue()
    }

    @Test
    fun `non existing user`() {
        every { userDataRepository.findByEmail("test@ebi.ac.uk") } returns null

        val exception = assertThrows<UserNotFoundByEmailException> { testInstance.getExtUser("test@ebi.ac.uk") }
        assertThat(exception.message).isEqualTo("Could not find user with the provided email 'test@ebi.ac.uk'.")
    }

    private fun mockUser(): DbUser {
        val user = mockk<DbUser>()
        every { user.login } returns "test"
        every { user.fullName } returns "Test User"
        every { user.email } returns "test@ebi.ac.uk"
        every { user.notificationsEnabled } returns true

        return user
    }
}
