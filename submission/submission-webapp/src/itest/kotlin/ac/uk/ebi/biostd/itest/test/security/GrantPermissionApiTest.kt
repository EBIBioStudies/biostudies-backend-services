package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@Import(FilePersistenceConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GrantPermissionApiTest(
    @Autowired private val userDataRepository: UserDataRepository,
    @Autowired private val accessPermissionRepository: AccessPermissionRepository,
    @Autowired private val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var superWebClient: BioWebClient
    private lateinit var regularWebClient: BioWebClient

    @BeforeAll
    fun init(): Unit =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureUserRegistration(RegularUser)

            superWebClient = getWebClient(serverPort, SuperUser)
            regularWebClient = getWebClient(serverPort, RegularUser)

            val collection =
                tsv {
                    line("Submission", "PermissionCollection")
                    line("AccNoTemplate", "!{S-PCOL}")
                    line()

                    line("Project")
                }.toString()
            superWebClient.submitSingle(collection, TSV)
        }

    @BeforeEach
    fun beforeAll() {
        userDataRepository.save(dbUser)
    }

    @Test
    fun `21-1 Grant permission to a Regular user by Superuser`() {
        superWebClient.grantPermission(dbUser.email, "PermissionCollection", "READ")

        val permissions = accessPermissionRepository.findAllByUserEmail(dbUser.email)

        assertThat(permissions).hasSize(1)
        assertThat(permissions.first().user.email).isEqualTo(dbUser.email)
        assertThat(permissions.first().accessTag.name).isEqualTo("PermissionCollection")
    }

    @Test
    fun `21-2 Grant permission to a Regular user by Regular user`() {
        assertThrows<WebClientException> {
            regularWebClient.grantPermission(dbUser.email, "PermissionCollection", "READ")
        }
    }

    @Test
    fun `21-3 Grant permission to non-existing user`() {
        assertThrows<WebClientException>("The user fakeUser does not exist") {
            superWebClient.grantPermission("fakeUser", "PermissionCollection", "READ")
        }
    }

    @Test
    fun `21-4 Grant permission to non-existing submission`() {
        assertThrows<WebClientException>("The submission fakeAccNo was not found") {
            superWebClient.grantPermission(dbUser.email, "fakeAccNo", "READ")
        }
    }

    private companion object {
        val dbUser =
            DbUser(
                email = "test@email.com",
                fullName = "fullName",
                secret = "secret",
                keyTime = 2,
                passwordDigest = ByteArray(1),
                storageMode = StorageMode.NFS,
            )
    }
}
