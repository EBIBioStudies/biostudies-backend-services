package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import ebi.ac.uk.api.security.RegisterRequest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension

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
    fun `register with invalid email`() {
        val request = RegisterRequest("Test", "not-a-mail", "123", captcha = "captcha")
        assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
            webClient.registerUser(request)
        }
    }

    @Test
    fun `register when activation is not enable`() {
        webClient.registerUser(NewUser.asRegisterRequest())
        webClient.getAuthenticatedClient(NewUser.email, NewUser.password)
    }

    object NewUser : TestUser {
        override val username = "New User"
        override val email = "new-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true

        override fun asRegisterRequest() = RegisterRequest(username, email, password)
    }
}
