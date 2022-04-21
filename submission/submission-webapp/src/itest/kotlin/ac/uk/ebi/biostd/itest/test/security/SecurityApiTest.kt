package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.clean
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.listener.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ebi.ac.uk.api.security.RegisterRequest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class SecurityApiTest(
    @Autowired var securityTestService: SecurityTestService,
    @Autowired private val accessTagRepository: AccessTagDataRepo,
    @Autowired private val accessPermissionRepository: AccessPermissionRepository,
    @Autowired private val sequenceDataRepository: SequenceDataRepository,
    @LocalServerPort val serverPort: Int
    ) {
    private lateinit var webClient: SecurityWebClient

    @BeforeAll
    fun init() {
        tempFolder.clean()

        sequenceDataRepository.deleteAll()
        accessPermissionRepository.deleteAll()
        accessTagRepository.deleteAll()
        securityTestService.deleteAllDbUsers()

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
