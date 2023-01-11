package ebi.ac.uk.security.util

import ac.uk.ebi.biostd.common.properties.InstanceKeys
import ac.uk.ebi.biostd.persistence.model.DbSecurityToken
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.security.test.SecurityTestEntities
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.adminUser
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.simpleUser
import io.jsonwebtoken.Jwts
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

private const val TOKEN_HASH = "ABC123"

private const val DEV_KEY = "dev-key"
private const val BETA_KEY = "beta-key"
private const val PROD_KEY = "prod-key"

@ExtendWith(MockKExtension::class)
class SecurityUtilTest(
    @MockK val userRepository: UserDataRepository,
    @MockK val tokenRepository: TokenDataRepository
) {
    private val instanceKeys = InstanceKeys(dev = DEV_KEY, beta = BETA_KEY, prod = PROD_KEY)

    private val testInstance =
        SecurityUtil(
            Jwts.parser(),
            JacksonFactory.createMapper(),
            tokenRepository,
            userRepository,
            TOKEN_HASH,
            instanceKeys
        )

    @Nested
    inner class TokenCases {
        @Test
        fun `token can be generated and converted back`() {
            val user = simpleUser
            every { userRepository.readByEmail(user.email) } returns user

            val token = testInstance.createToken(user)

            assertThat(testInstance.fromToken(token)).isEqualTo(user)
        }

        @Test
        fun `from token when invalid token`() {
            assertThat(testInstance.fromToken("invalid_token")).isNull()
        }

        @Test
        fun `from token when invalid signature`() {
            val securityUtil =
                SecurityUtil(
                    Jwts.parser(),
                    JacksonFactory.createMapper(),
                    tokenRepository,
                    userRepository,
                    "another_hash",
                    instanceKeys
                )

            assertThat(testInstance.fromToken(securityUtil.createToken(simpleUser))).isNull()
        }
    }

    @Nested
    inner class PasswordCases {
        @Test
        fun `check password when match`() {
            val password = "abc123"
            val passwordDigest = testInstance.getPasswordDigest(password)

            assertThat(testInstance.checkPassword(passwordDigest, password)).isTrue
        }

        @Test
        fun `check password is set as valid when super user security token is used`() {
            val superUserToken = testInstance.createToken(adminUser)
            every { userRepository.readByEmail(SecurityTestEntities.email) } returns adminUser

            assertThat(testInstance.checkPassword(ByteArray(1), superUserToken)).isTrue
        }

        @Test
        fun `check password is set as invalid when normal user security token is used`() {
            val userToken = testInstance.createToken(simpleUser)
            every { userRepository.getById(SecurityTestEntities.userId) } returns simpleUser

            assertThat(testInstance.checkPassword(ByteArray(1), userToken)).isFalse
        }

        @Test
        fun getPasswordDigest() {
            assertThat(testInstance.getPasswordDigest("abc")).isNotEmpty
        }
    }

    @Nested
    inner class CheckToken {
        private val securityToken = mockk<DbSecurityToken>()

        @Test
        fun `check when is not in black list`() {
            val myToken = "acb123"
            every { tokenRepository.findById(myToken) } returns Optional.of(securityToken)

            assertThat(testInstance.checkToken(myToken)).isNull()
        }

        @Test
        fun `check when exist`() {
            val user = simpleUser

            val token = testInstance.createToken(user)

            every { userRepository.readByEmail(user.email) } returns user
            every { tokenRepository.findById(token) } returns Optional.empty()

            assertThat(testInstance.checkToken(token)).isEqualTo(user)
        }
    }

    @Nested
    inner class GetActivationUrl {
        private val userKey = "abc123"
        private val paths = listOf("/autenticate/ui-link", "/autenticate/ui-link/", "autenticate/ui-link")

        @TestFactory
        fun testActivationKey(): Collection<DynamicTest> =
            paths.flatMap {
                listOf(
                    test(it, DEV_KEY, DEV_INSTANCE),
                    test(it, BETA_KEY, BETA_INSTANCE),
                    test(it, PROD_KEY, PROD_INSTANCE),
                    test(it, "http://localhost", "http://localhost"),
                    test(it, "https://localhost", "https://localhost")
                )
            }

        @Test
        fun testWhenInvalidInstance() {
            assertThrows<IllegalArgumentException> {
                testInstance.getActivationUrl("https://custom-server", "a path", userKey)
            }
        }

        private fun test(path: String, instanceKey: String, expectedInstance: String): DynamicTest =
            dynamicTest("when InstanceKey=$instanceKey, path=$path and activationKey=$userKey") {
                testUrl(path, instanceKey, expectedInstance)
            }

        private fun testUrl(path: String, instanceKey: String, expectedInstance: String) {
            val url = testInstance.getActivationUrl(instanceKey, path, userKey)
            assertThat(url).isEqualTo("$expectedInstance/autenticate/ui-link/abc123")
        }
    }
}
