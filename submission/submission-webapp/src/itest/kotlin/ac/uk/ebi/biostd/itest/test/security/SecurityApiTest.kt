package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.api.security.RegisterRequest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

internal class SecurityApiTest {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SecurityApiTest(
        @Autowired var securityTestService: SecurityTestService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: SecurityWebClient

        @BeforeAll
        fun init() {
            securityTestService.deleteSuperUser()
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
            webClient.registerUser(SuperUser.asRegisterRequest())
            webClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
        }
    }
}
