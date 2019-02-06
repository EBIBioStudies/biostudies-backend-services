package ebi.ac.uk.security.util

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.commons.http.JacksonFactory
import io.jsonwebtoken.Jwts
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class TokenUtilTest(@MockK val userRepository: UserDataRepository) {

    private val testInstance = TokenUtil(Jwts.parser(), JacksonFactory.createMapper(), userRepository, "token_hash")

    @Test
    fun `token can be generated and converted back`() {
        val user = createUser()
        every { userRepository.getOne(50L) } returns user

        val token = testInstance.createToken(createUser())
        assertThat(testInstance.fromToken(token)).contains(user)
    }

    @Test
    fun `from token when invalid token`() {
        assertThat(testInstance.fromToken("invalid_token")).isEmpty()
    }

    @Test
    fun `from token when invalid signature`() {
        assertThat(testInstance.fromToken(createToken("another_hash"))).isEmpty()
    }

    private fun createToken(hash: String) =
        TokenUtil(Jwts.parser(), JacksonFactory.createMapper(), userRepository, hash).createToken(createUser())

    private fun createUser(): User {
        val user = User("user", "user@not-existing.com", "abc")
        user.id = 50L
        return user
    }
}
