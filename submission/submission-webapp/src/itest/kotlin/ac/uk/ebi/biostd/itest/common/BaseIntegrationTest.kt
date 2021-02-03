package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.db.MYSQL_VERSION
import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

private const val CHARACTER_SET = "utf8mb4"
private const val COLLATION = "utf8mb4_unicode_ci"

internal open class BaseIntegrationTest(
    private val tempFolder: TemporaryFolder
) {
    private val mysqlContainer = SpecifiedMySQLContainer(MYSQL_VERSION)
        .withCommand("mysqld --character-set-server=$CHARACTER_SET --collation-server=$COLLATION")

    private val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))

    val submissionPath
        get() = "${tempFolder.root.absolutePath}/submission"

    @BeforeAll
    fun beforeAll() {
        if (System.getProperty("itest.mode") == "mongo") {
            setUpMongo()
        }

        setUpMySql()
        setUpApplicationProperties()
    }

    @AfterAll
    fun afterAll() {
        mysqlContainer.stop()
        mongoContainer.stop()
    }

    protected fun getWebClient(serverPort: Int, user: TestUser): BioWebClient {
        val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
        return securityClient.getAuthenticatedClient(user.email, user.password)
    }

    protected fun createUser(testUser: TestUser, serverPort: Int) {
        SecurityWebClient.create("http://localhost:$serverPort").registerUser(testUser.asRegisterRequest())
    }

    private fun setUpMongo() {
        mongoContainer.start()
        System.setProperty("spring.data.mongodb.uri", mongoContainer.getReplicaSetUrl("biostudies-test"))
        System.setProperty("spring.data.mongodb.database", "biostudies-test")
        System.setProperty("app.persistence.enableMongo", "true")
    }

    private fun setUpMySql() {
        mysqlContainer.start()
        System.setProperty("spring.datasource.url", mysqlContainer.jdbcUrl)
        System.setProperty("spring.datasource.username", mysqlContainer.username)
        System.setProperty("spring.datasource.password", mysqlContainer.password)
    }

    private fun setUpApplicationProperties() {
        System.setProperty("app.submissionPath", submissionPath)
        System.setProperty("app.ftpPath", "${tempFolder.root.absolutePath}/ftpPath")
        System.setProperty("app.tempDirPath", tempFolder.createDirectory("tmp").absolutePath)
        System.setProperty("app.security.filesDirPath", tempFolder.createDirectory("dropbox").absolutePath)
        System.setProperty("app.security.magicDirPath", tempFolder.createDirectory("magic").absolutePath)
    }
}

internal class SpecifiedMySQLContainer(image: String) : MySQLContainer<SpecifiedMySQLContainer>(image)
