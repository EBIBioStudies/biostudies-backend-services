package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.MySQLContainer

private const val CHARACTER_SET = "utf8mb4"
private const val COLLATION = "utf8mb4_unicode_ci"
private const val MYSQL_IMAGE = "mysql:5.7.33"

internal open class BaseIntegrationTest(
    private val tempFolder: TemporaryFolder
) {
    private val mysqlContainer = SpecifiedMySQLContainer(MYSQL_IMAGE)
        .withCommand("mysqld --character-set-server=$CHARACTER_SET --collation-server=$COLLATION")

    val submissionPath
        get() = "${tempFolder.root.absolutePath}/submission"

    @BeforeAll
    fun beforeAll() {
        mysqlContainer.start()

        System.setProperty("app.submissionPath", submissionPath)
        System.setProperty("app.ftpPath", "${tempFolder.root.absolutePath}/ftpPath")
        System.setProperty("app.tempDirPath", tempFolder.createDirectory("tmp").absolutePath)
        System.setProperty("app.security.filesDirPath", tempFolder.createDirectory("dropbox").absolutePath)
        System.setProperty("app.security.magicDirPath", tempFolder.createDirectory("magic").absolutePath)
        System.setProperty("spring.datasource.url", mysqlContainer.jdbcUrl)
        System.setProperty("spring.datasource.username", mysqlContainer.username)
        System.setProperty("spring.datasource.password", mysqlContainer.password)
    }

    @AfterAll
    fun afterAll() {
        mysqlContainer.stop()
    }

    protected fun getWebClient(serverPort: Int, user: TestUser): BioWebClient {
        val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
        return securityClient.getAuthenticatedClient(user.email, user.password)
    }

    protected fun createUser(testUser: TestUser, serverPort: Int) {
        SecurityWebClient.create("http://localhost:$serverPort").registerUser(testUser.asRegisterRequest())
    }
}

internal class SpecifiedMySQLContainer(image: String) : MySQLContainer<SpecifiedMySQLContainer>(image)
