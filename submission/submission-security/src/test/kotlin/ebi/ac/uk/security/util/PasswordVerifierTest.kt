package ebi.ac.uk.security.util

import ac.uk.ebi.biostd.persistence.model.User
import arrow.core.Option
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class PasswordVerifierTest(@MockK val tokenUtil: TokenUtil) {

    private val testInstance: PasswordVerifier = PasswordVerifier(tokenUtil)

    @Test
    fun `check password when match`() {
        val password = "abc123"
        val passwordDigest = testInstance.getPasswordDigest(password)
        every { tokenUtil.fromToken(password) } returns Option.empty()

        assertThat(testInstance.checkPassword(passwordDigest, password)).isTrue()
    }

    @Test
    fun `check password is set as valid when super user security token is used`() {
        val managerUser = createUser(true)

        val managerToken = "a_security_token"
        every { tokenUtil.fromToken(managerToken) } returns Option.just(managerUser)

        assertThat(testInstance.checkPassword(ByteArray(1), managerToken)).isTrue()
    }

    @Test
    fun `check password is set as valid when normal user security token is used`() {
        val managerUser = createUser(false)

        val managerToken = "a_security_token"
        every { tokenUtil.fromToken(managerToken) } returns Option.just(managerUser)

        assertThat(testInstance.checkPassword(ByteArray(1), managerToken)).isFalse()
    }

    private fun createUser(superuser: Boolean): User {
        val managerUser = User("user", "user@not-existing.com", "abc")
        managerUser.superuser = superuser
        return managerUser
    }

    @Test
    fun getPasswordDigest() {
        assertThat(testInstance.getPasswordDigest("abc")).isNotEmpty()
    }
}
