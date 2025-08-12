package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.model.AccessType.DELETE
import ac.uk.ebi.biostd.persistence.common.model.AccessType.READ
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.util.collections.second
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
class UserPermissionsApiTest(
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
            superWebClient.submit(collection, TSV)
        }

    @BeforeEach
    fun beforeAll() {
        userDataRepository.save(dbUser)
    }

    @Test
    fun `21-1 Grant permission to a Regular user by Superuser`() =
        runTest {
            superWebClient.grantPermission(dbUser.email, "PermissionCollection", READ.name)

            val permissions = accessPermissionRepository.findAllByUserEmail(dbUser.email)

            assertThat(permissions).hasSize(1)
            assertPermission(permissions.first(), READ)
        }

    @Test
    fun `21-2 Grant permission to a Regular user by Regular user`() =
        runTest {
            assertThrows<WebClientException> {
                regularWebClient.grantPermission(dbUser.email, "PermissionCollection", READ.name)
            }
        }

    @Test
    fun `21-3 Grant permission to non-existing user`() =
        runTest {
            assertThrows<WebClientException>("The user fakeUser does not exist") {
                superWebClient.grantPermission("fakeUser", "PermissionCollection", READ.name)
            }
        }

    @Test
    fun `21-4 Grant permission to non-existing submission`() =
        runTest {
            assertThrows<WebClientException>("The submission fakeAccNo was not found") {
                superWebClient.grantPermission(dbUser.email, "fakeAccNo", READ.name)
            }
        }

    @Test
    fun `21-5 Revoke permissions to a Regular user by Superuser`() =
        runTest {
            superWebClient.grantPermission(dbUser.email, "PermissionCollection", READ.name)
            superWebClient.grantPermission(dbUser.email, "PermissionCollection", DELETE.name)

            val permissions = accessPermissionRepository.findAllByUserEmail(dbUser.email)
            assertThat(permissions).hasSize(2)
            assertPermission(permissions.first(), READ)
            assertPermission(permissions.second(), DELETE)

            superWebClient.revokePermission(dbUser.email, "PermissionCollection", READ.name)

            val updatedPermissions = accessPermissionRepository.findAllByUserEmail(dbUser.email)
            assertThat(updatedPermissions).hasSize(1)
            assertPermission(updatedPermissions.first(), DELETE)
        }

    private fun assertPermission(
        permission: DbAccessPermission,
        accessType: AccessType,
    ) {
        assertThat(permission.accessType).isEqualTo(accessType)
        assertThat(permission.user.email).isEqualTo(dbUser.email)
        assertThat(permission.accessTag.name).isEqualTo("PermissionCollection")
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
