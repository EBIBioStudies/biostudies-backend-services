package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.db.MYSQL_VERSION
import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.utility.DockerImageName
import uk.ac.ebi.fire.client.api.FIRE_OBJECTS_URL
import java.time.Duration.ofSeconds

private const val CHARACTER_SET = "utf8mb4"
private const val COLLATION = "utf8mb4_unicode_ci"
private const val FIRE_USERNAME = "fireUsername"
private const val FIRE_PASSWORD = "firePassword"

internal open class BaseIntegrationTest(private val tempFolder: TemporaryFolder) {
    private val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
        .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

    private val mysqlContainer = SpecificMySQLContainer(MYSQL_VERSION)
        .withCommand("mysqld --character-set-server=$CHARACTER_SET --collation-server=$COLLATION")
        .withInitScript("Schema.sql")
        .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))
    private val testWireMockTransformer = TestWireMockTransformer(tempFolder.createDirectory("fireFolder"))
    private val wireMockFireFilesServer = WireMockServer(
        WireMockConfiguration().dynamicPort().extensions(testWireMockTransformer)
    )
    val submissionPath
        get() = "${tempFolder.root.absolutePath}/submission"

    @BeforeAll
    fun beforeAll() {
        if (System.getProperty("itest.mode") == "mongo") {
            setUpMongo()
        }

        setUpMySql()
        setUpApplicationProperties()
        if (System.getProperty("app.persistence.enableFire") == "true") {

            wireMockFireFilesServer.stubFor(
                post(urlMatching(FIRE_OBJECTS_URL))
                    .withBasicAuth(FIRE_USERNAME, FIRE_PASSWORD)
                    .willReturn(aResponse().withTransformers(testWireMockTransformer.name))
            )
            wireMockFireFilesServer.start()
            setupFireFileSystem()
        }
    }

    private fun setupFireFileSystem() {
        System.setProperty("app.fire.host", wireMockFireFilesServer.baseUrl())
        System.setProperty("app.fire.username", FIRE_USERNAME)
        System.setProperty("app.fire.password", FIRE_PASSWORD)
    }

    @AfterAll
    fun afterAll() {
        mysqlContainer.stop()
        mongoContainer.stop()
        wireMockFireFilesServer.stop()
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
        System.setProperty("app.persistence.enableFire", "true")
    }
}

internal class SpecificMySQLContainer(image: String) : MySQLContainer<SpecificMySQLContainer>(image)
