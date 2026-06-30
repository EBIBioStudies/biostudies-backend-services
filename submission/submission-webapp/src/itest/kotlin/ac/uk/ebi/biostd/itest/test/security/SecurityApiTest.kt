package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.FtpSuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.api.security.RegisterRequest
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.SECONDS

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityApiTest(
    @param:LocalServerPort val serverPort: Int,
    @param:Autowired private val userDataRepository: UserDataRepository,
    @param:Autowired private val securityTestService: SecurityTestService,
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
    fun `22-6 check ftp home type user`() =
        runTest {
            securityTestService.ensureUserRegistration(FtpSuperUser)
            val client = getWebClient(serverPort, FtpSuperUser)

            val result = client.getProfile()

            assertThat(result.uploadType).isEqualTo("ftp")
        }

    @Test
    fun `22-7 check Nfs home type user`() =
        runTest {
            securityTestService.ensureUserRegistration(NfsUser)
            val client = getWebClient(serverPort, NfsUser)

            val result = client.getProfile()

            assertThat(result.uploadType).isEqualTo("nfs")
        }

    @Test
    fun `22-8 get user profile`() =
        runTest {
            securityTestService.ensureUserRegistration(FtpSuperUser)
            val client = getWebClient(serverPort, FtpSuperUser)

            val startedAt = LocalDateTime.now().minusSeconds(1).truncatedTo(SECONDS)
            val result = client.getProfile()
            val finishedAt = LocalDateTime.now().plusSeconds(1).truncatedTo(SECONDS)

            val storedUser = userDataRepository.getByEmail(FtpSuperUser.email)

            assertThat(result.email).isEqualTo(FtpSuperUser.email)
            assertThat(result.fullname).isEqualTo(FtpSuperUser.username)
            assertThat(result.uploadType).isEqualTo("ftp")
            assertThat(result.lastActivity.truncatedTo(SECONDS)).isBetween(startedAt, finishedAt)
            assertThat(storedUser.lastActivity.truncatedTo(SECONDS)).isBetween(startedAt, finishedAt)
        }

    object NfsUser : TestUser {
        override val username = "New Nfs User"
        override val email = "new-nfs-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
        override val storageMode = StorageMode.NFS
    }

    object NewUser : TestUser {
        override val username = "New User"
        override val email = "new-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
        override val storageMode = StorageMode.NFS
    }
}
