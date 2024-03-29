package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
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
class PermissionApiTest(
    @Autowired private val userDataRepository: UserDataRepository,
    @Autowired private val accessTagRepository: AccessTagDataRepo,
    @Autowired private val accessPermissionRepository: AccessPermissionRepository,
    @Autowired private val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var superWebClient: BioWebClient
    private lateinit var regularWebClient: BioWebClient

    @BeforeAll
    fun init(): Unit = runBlocking {
        securityTestService.ensureUserRegistration(SuperUser)
        securityTestService.ensureUserRegistration(RegularUser)

        superWebClient = getWebClient(serverPort, SuperUser)
        regularWebClient = getWebClient(serverPort, RegularUser)

        accessTagRepository.save(dbAccessTag)
    }

    @BeforeEach
    fun beforeAll() {
        userDataRepository.save(dbUser)
    }

    @Test
    fun `21-1 give permission to a user by superUser`() {
        superWebClient.givePermissionToUser(dbUser.email, dbAccessTag.name, "READ")

        val permissions = accessPermissionRepository.findAllByUserEmail(dbUser.email)

        assertThat(permissions).hasSize(1)
        assertThat(permissions.first().user.email).isEqualTo(dbUser.email)
        assertThat(permissions.first().accessTag.name).isEqualTo(dbAccessTag.name)
    }

    @Test
    fun `21-2 trying to give permission to a user by regularUser`() {
        assertThrows<WebClientException> {
            regularWebClient.givePermissionToUser(dbUser.email, dbAccessTag.name, "READ")
        }
    }

    @Test
    fun `21-3 trying to give permission to non-existent user`() {
        assertThrows<WebClientException>("The user $fakeUser does not exist") {
            superWebClient.givePermissionToUser(fakeUser, dbAccessTag.name, "READ")
        }
    }

    @Test
    fun `21-4 trying to give permission to a user but non-existent accessTag`() {
        assertThrows<WebClientException>("The accessTag $fakeAccessTag does not exist") {
            superWebClient.givePermissionToUser(dbUser.email, fakeAccessTag, "READ")
        }
    }

    private companion object {
        val dbUser = DbUser(
            email = "test@email.com",
            fullName = "fullName",
            secret = "secret",
            keyTime = 2,
            passwordDigest = ByteArray(1),
            storageMode = StorageMode.NFS
        )
        val dbAccessTag = DbAccessTag(id = 2, name = "accessTagName")
        const val fakeUser = "fakeUser"
        const val fakeAccessTag = "fakeAccessTag"
    }
}
