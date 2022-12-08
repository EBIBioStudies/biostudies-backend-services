package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import ebi.ac.uk.api.security.CheckUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.ResourceAccessException

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityApiTest(
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: SecurityWebClient

    @BeforeAll
    fun init() {
        webClient = SecurityWebClient.create("http://localhost:$serverPort")
    }

    @Test
    fun `22-1 register with invalid email`() {
        val request = RegisterRequest("Test", "not-a-mail", "123", captcha = "captcha")
        assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
            webClient.registerUser(request)
        }
    }

    @Test
    fun `22-2 register when activation is not enable`() {
        webClient.registerUser(NewUser.asRegisterRequest())
        webClient.getAuthenticatedClient(NewUser.email, NewUser.password)
    }

    @Test
    fun `22-3 login when inactive`() {
        webClient.checkUser(CheckUserRequest(InactiveUser.email, InactiveUser.username))

        assertThatExceptionOfType(ResourceAccessException::class.java).isThrownBy {
            webClient.login(LoginRequest(InactiveUser.email, InactiveUser.password))
        }
    }

    object NewUser : TestUser {
        override val username = "New User"
        override val email = "new-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true

        override fun asRegisterRequest() = RegisterRequest(username, email, password)
    }

    object InactiveUser : TestUser {
        override val username = "Inactive User"
        override val email = "inactive@ebi.ac.uk"
        override val password = "12345"
        override val superUser = false

        override fun asRegisterRequest() = RegisterRequest(username, email, password)
    }
}
