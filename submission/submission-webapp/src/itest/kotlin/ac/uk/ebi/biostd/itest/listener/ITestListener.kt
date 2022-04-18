package ac.uk.ebi.biostd.itest.listener

import ac.uk.ebi.biostd.itest.common.SpecificMySQLContainer
import ac.uk.ebi.biostd.itest.common.TestWireMockTransformer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.db.MYSQL_VERSION
import ebi.ac.uk.io.ext.createDirectory
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.time.Duration.ofSeconds
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan
import org.springframework.util.StopWatch
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.utility.DockerImageName
import uk.ac.ebi.fire.client.api.FIRE_OBJECTS_URL

private const val CHARACTER_SET = "utf8mb4"
private const val COLLATION = "utf8mb4_unicode_ci"

private const val FIRE_USERNAME = "fireUsername"
private const val FIRE_PASSWORD = "firePassword"

class ITestListener : TestExecutionListener {
    private val stopWatch = StopWatch("ITests")
    private val mongoContainer: MongoDBContainer =
        MongoDBContainer(DockerImageName.parse(MONGO_VERSION)).withStartupCheckStrategy(
            MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))
    private val mysqlContainer =
        SpecificMySQLContainer(MYSQL_VERSION)
            .withCommand("mysqld --character-set-server=$CHARACTER_SET --collation-server=$COLLATION")
            .withInitScript("Schema.sql")
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        stopWatch.start()
        println("#########   ITestListener.testPlanExecutionStarted")

        mongoSetup()
        mySqlSetup()
        fireSetup()
        appPropertiesSetup()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        mongoContainer.stop()
        mysqlContainer.stop()
        fireWireMock.stop()

        stopWatch.stop()
        println("#########   ITestListener.testPlanExecutionFinished")
        println("seconds : " + stopWatch.totalTimeSeconds)
    }

    private fun mongoSetup() {
        val mongoMode = System.getProperty("itest.mode") == "mongo"

        if (mongoMode) {
            mongoContainer.start()
            System.setProperty("spring.data.mongodb.uri", mongoContainer.getReplicaSetUrl("biostudies-test"))
            System.setProperty("spring.data.mongodb.database", "biostudies-test")
        }
    }

    private fun mySqlSetup() {
        mysqlContainer.start()
        System.setProperty("spring.datasource.url", mysqlContainer.jdbcUrl)
        System.setProperty("spring.datasource.username", mysqlContainer.username)
        System.setProperty("spring.datasource.password", mysqlContainer.password)
    }

    private fun fireSetup() {
        fireWireMock.stubFor(
            WireMock.post(WireMock.urlMatching(FIRE_OBJECTS_URL))
                .withBasicAuth(FIRE_USERNAME, FIRE_PASSWORD)
                .willReturn(WireMock.aResponse().withTransformers(wireMockTransformer.name))
        )
        fireWireMock.start()

        System.setProperty("app.fire.host", fireWireMock.baseUrl())
        System.setProperty("app.fire.username", FIRE_USERNAME)
        System.setProperty("app.fire.password", FIRE_PASSWORD)
    }

    private fun appPropertiesSetup() {
        val tempDirPath = tempFolder.createDirectory("tmp")
        System.setProperty("app.submissionPath", submissionPath)
        System.setProperty("app.ftpPath", "${tempFolder.absolutePath}/ftpPath")
        System.setProperty("app.fireTempDirPath", fireTempFolder)
        System.setProperty("app.tempDirPath", tempDirPath.absolutePath)
        System.setProperty("app.requestFilesPath", tempFolder.createDirectory("request-files").absolutePath)

        System.setProperty("app.security.filesDirPath", tempFolder.createDirectory("dropbox").absolutePath)
        System.setProperty("app.security.magicDirPath", tempFolder.createDirectory("magic").absolutePath)
        System.setProperty("app.persistence.enableFire", "${System.getProperty("enableFire").toBoolean()}")
    }

    companion object {
        val tempFolder: File = Files.createTempDirectory("tempFolder").toFile()
        private val wireMockTransformer = TestWireMockTransformer(tempFolder.createDirectory("submission"))
        private val fireWireMock = WireMockServer(WireMockConfiguration().dynamicPort().extensions(wireMockTransformer))

        private val fireTempFolder
            get() = "${System.getProperty("app.tempDirPath")}/fire-temp"
        val submissionPath
            get() = "${tempFolder.absolutePath}/submission"
    }
}
