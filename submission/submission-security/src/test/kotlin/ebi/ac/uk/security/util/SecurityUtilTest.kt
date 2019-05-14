package ebi.ac.uk.security.util

import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.security.test.SecurityTestEntities
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.adminUser
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.simpleUser
import io.jsonwebtoken.Jwts
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val tokenHash = "ABC123"

@ExtendWith(MockKExtension::class)
class SecurityUtilTest(@MockK val userRepository: UserDataRepository) {
    private val testInstance = SecurityUtil(Jwts.parser(), JacksonFactory.createMapper(), userRepository, tokenHash)

    @Nested
    inner class TokenCases {
        @Test
        fun `token can be generated and converted back`() {
            every { userRepository.getOne(simpleUser.id) } returns simpleUser

            val token = testInstance.createToken(simpleUser)
            assertThat(testInstance.fromToken(token)).contains(simpleUser)
        }

        @Test
        fun `from token when invalid token`() {
            assertThat(testInstance.fromToken("invalid_token")).isEmpty()
        }

        @Test
        fun `from token when invalid signature`() {
            val securityUtil = SecurityUtil(Jwts.parser(), JacksonFactory.createMapper(), userRepository, "another_hash")

            assertThat(testInstance.fromToken(securityUtil.createToken(simpleUser))).isEmpty()
        }
    }

    @Nested
    inner class PasswordCases {
        @Test
        fun `check password when match`() {
            val password = "abc123"
            val passwordDigest = testInstance.getPasswordDigest(password)

            assertThat(testInstance.checkPassword(passwordDigest, password)).isTrue()
        }

        @Test
        fun `check password is set as valid when super user security token is used`() {
            val superUserToken = testInstance.createToken(adminUser)
            every { userRepository.getOne(SecurityTestEntities.adminId) } returns adminUser

            assertThat(testInstance.checkPassword(ByteArray(1), superUserToken)).isTrue()
        }

        @Test
        fun `check password is set as invalid when normal user security token is used`() {
            val userToken = testInstance.createToken(simpleUser)
            every { userRepository.getOne(SecurityTestEntities.userId) } returns simpleUser

            assertThat(testInstance.checkPassword(ByteArray(1), userToken)).isFalse()
        }

        @Test
        fun getPasswordDigest() {
            assertThat(testInstance.getPasswordDigest("abc")).isNotEmpty()
        }
    }
}
