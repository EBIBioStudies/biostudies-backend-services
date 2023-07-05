package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.api.security.CheckUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertNotNull

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityApiTest(
    @LocalServerPort val serverPort: Int,
    @Autowired private val userDataRepository: UserDataRepository,
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

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { webClient.login(LoginRequest(InactiveUser.email, InactiveUser.password)) }
            .withMessageContaining("Authentication Error: Invalid email address or password")
    }

    @Test
    fun `22-4 case insensitive user registration`() {
        val request = RegisterRequest("User", "Case-Insensitive@mail.org", "123")
        webClient.registerUser(request)

        val user = userDataRepository.findByEmailAndActive("Case-Insensitive@mail.org", true)
        assertNotNull(user)
        assertThat(user.email).isEqualTo("case-insensitive@mail.org")
    }

    @Test
    fun `22-5 case insensitive inactive registration`() {
        val request = CheckUserRequest("Case-Insensitive-Inactive@mail.org", "User")
        webClient.checkUser(request)

        val user = userDataRepository.findByEmailAndActive("Case-Insensitive-Inactive@mail.org", false)
        assertNotNull(user)
        assertThat(user.email).isEqualTo("case-insensitive-inactive@mail.org")
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
