package uk.ac.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.exception.UserWithActivationKeyNotFoundException
import ebi.ac.uk.security.service.SecurityService
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
import uk.ac.ebi.biostd.api.security.ChangePasswordRequest
import uk.ac.ebi.biostd.api.security.RegisterRequest
import uk.ac.ebi.biostd.api.security.ResetPasswordRequest
import uk.ac.ebi.biostd.client.exception.WebClientException
import uk.ac.ebi.biostd.client.integration.web.SecurityWebClient
import uk.ac.ebi.biostd.common.properties.StorageMode
import uk.ac.ebi.biostd.itest.common.SecurityTestService
import uk.ac.ebi.biostd.itest.entities.FtpSuperUser
import uk.ac.ebi.biostd.itest.entities.TestUser
import uk.ac.ebi.biostd.itest.itest.getWebClient
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.SECONDS
import kotlin.test.assertFailsWith

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityApiTest(
    @param:LocalServerPort val serverPort: Int,
    @param:Autowired private val userDataRepository: UserDataRepository,
    @param:Autowired private val securityTestService: SecurityTestService,
    @param:Autowired private val securityService: SecurityService,
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

    @Test
    fun `22-9 reset password activates inactive user and consumes reset key`() =
        runTest {
            securityTestService.ensureUserRegistration(ResetUser)
            val user = userDataRepository.getByEmail(ResetUser.email)
            user.active = false
            userDataRepository.save(user)
            val newPassword = "new-password-123"

            assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
                webClient.getAuthenticatedClient(ResetUser.email, ResetUser.password)
            }
            securityService.resetPassword(ResetPasswordRequest(ResetUser.email, "http://localhost:8080", "/reset"))

            val pendingUser = userDataRepository.getByEmail(ResetUser.email)
            assertThat(pendingUser.active).isFalse()
            val key = requireNotNull(pendingUser.activationKey)
            val request = ChangePasswordRequest(key, newPassword)

            securityService.changePassword(request)
            val activatedUser = userDataRepository.getByEmail(ResetUser.email)
            assertThat(activatedUser.active).isTrue()
            assertThat(activatedUser.activationKey).isNull()
            webClient.getAuthenticatedClient(ResetUser.email, newPassword)
            assertFailsWith<UserWithActivationKeyNotFoundException> { securityService.changePassword(request) }
        }

    object ResetUser : TestUser {
        override val username = "Password Reset User"
        override val email = "password-reset-user@ebi.ac.uk"
        override val password = "12345"
        override val superUser = false
        override val storageMode = StorageMode.NFS
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
