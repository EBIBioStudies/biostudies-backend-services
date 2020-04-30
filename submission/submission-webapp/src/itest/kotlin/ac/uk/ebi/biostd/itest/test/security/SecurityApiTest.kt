package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.SecurityWebClientException
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.InvalidUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
internal class SecurityApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SecurityApiTest {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: SecurityWebClient

        @BeforeAll
        fun init() {
            webClient = SecurityWebClient.create("http://localhost:$serverPort")
        }

        @Test
        fun `register when activation is not enable`() {
            webClient.registerUser(SuperUser.asRegisterRequest())
            webClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
        }

        @Test
        fun `register with invalid email`() {
            assertThatExceptionOfType(SecurityWebClientException::class.java)
                .isThrownBy { webClient.registerUser(InvalidUser.asRegisterRequest()) }
        }
    }
}
