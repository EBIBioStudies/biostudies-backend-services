package ac.uk.ebi.biostd.itest.test.user

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.MigrateHomeOptions
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class UserAdminApiTest(
    @Autowired private val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var superWebClient: BioWebClient
    private lateinit var userWebClient: BioWebClient

    @BeforeEach
    fun beforeEach() =
        runBlocking {
            securityTestService.ensureUserRegistration(SimpleUser)
            securityTestService.ensureUserRegistration(SuperUser)

            superWebClient = getWebClient(serverPort, SuperUser)
            userWebClient = getWebClient(serverPort, SimpleUser)

            securityTestService.cleanUserFolder(SimpleUser)
        }

    @Test
    fun `30-1 Get ext user`() =
        runTest {
            val user = superWebClient.getExtUser(SimpleUser.email)

            assertThat(user.email).isEqualTo(SimpleUser.email)
        }

    @Test
    fun `30-2 Get user home stats`() =
        runTest {
            userWebClient.createFolder("main")
            userWebClient.createFolder("sub1", "/main")

            userWebClient.uploadFile(tempFolder.createFile("main.txt", "content"), relativePath = "/main")
            userWebClient.uploadFile(tempFolder.createFile("main-sub1.txt", "content sub"), relativePath = "main/sub1")

            val result = superWebClient.getUserHomeStats(SimpleUser.email)
            assertThat(result.totalFiles).isEqualTo(2)
            assertThat(result.totalFilesSize).isEqualTo(18)
            assertThat(result.totalDirectories).isEqualTo(2)
        }

    @Test
    fun `30-3 Migrate user folder when not empty folder and disable`() =
        runTest {
            userWebClient.uploadFile(tempFolder.createFile("main.txt", "content"))

            val options = MigrateHomeOptions("FTP", onlyIfEmptyFolder = true)

            val exception =
                assertThrows<WebClientException> {
                    superWebClient.migrateUser(SimpleUser.email, options)
                }
            assertThat(exception.message).contains("simple-user-123@ebi.ac.uk is not empty and can not be migrated")
        }

    @Test
    fun `30-4 Migrate user folder when not empty folder and enable`() =
        runTest {
            val testUser =
                object : TestUser {
                    override val username: String = "user30-4"
                    override val email: String = "user30-4@ebi.ac.uk"
                    override val password: String = "password"
                    override val superUser: Boolean = false
                    override val storageMode: StorageMode = StorageMode.NFS
                }

            securityTestService.ensureUserRegistration(testUser)
            val ftpUserClient = getWebClient(serverPort, testUser)

            ftpUserClient.createFolder("f1")
            ftpUserClient.uploadFile(tempFolder.createFile("1.txt", "content-1"), "f1")
            ftpUserClient.uploadFile(tempFolder.createFile("2.txt", "content-2"), "f1")
            ftpUserClient.uploadFile(tempFolder.createFile("3.txt", "content-3"))

            val homePath = securityTestService.getSecurityUser(testUser.email).userFolder.path
            Files.setLastModifiedTime(homePath.resolve("f1/1.txt"), FileTime.from(Instant.now().minus(5, HOURS)))
            Files.setLastModifiedTime(homePath.resolve("f1/2.txt"), FileTime.from(Instant.now().minus(30, HOURS)))
            Files.setLastModifiedTime(homePath.resolve("3.txt"), FileTime.from(Instant.now().minus(1, HOURS)))

            val options = MigrateHomeOptions("FTP", onlyIfEmptyFolder = false, copyFilesSinceDays = 1)

            superWebClient.migrateUser(testUser.email, options)

            val user = securityTestService.getSecurityUser(testUser.email)
            assertThat(user.userFolder).isInstanceOf(FtpUserFolder::class.java)

            assertThat(ftpUserClient.listUserFiles().map { it.name }).containsExactlyInAnyOrder("f1", "3.txt")
            assertThat(ftpUserClient.listUserFiles("f1").map { it.name }).containsOnly("1.txt")
        }

    @Test
    fun `30-5 Migrate user folder when empty folder`() =
        runTest {
            val options = MigrateHomeOptions("FTP", onlyIfEmptyFolder = true)

            superWebClient.migrateUser(SimpleUser.email, options)

            val user = securityTestService.getSecurityUser(SimpleUser.email)
            assertThat(user.userFolder).isInstanceOf(FtpUserFolder::class.java)
        }

    object SimpleUser : TestUser {
        override val username = "Simpler user 123"
        override val email = "simple-user-123@ebi.ac.uk"
        override val password = "12345"
        override val superUser = false
        override val storageMode: StorageMode = StorageMode.NFS
    }
}
