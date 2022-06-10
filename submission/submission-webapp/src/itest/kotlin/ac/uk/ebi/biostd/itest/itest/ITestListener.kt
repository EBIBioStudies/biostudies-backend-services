package ac.uk.ebi.biostd.itest.itest

import ac.uk.ebi.biostd.itest.common.CHARACTER_SET
import ac.uk.ebi.biostd.itest.common.COLLATION
import ac.uk.ebi.biostd.itest.common.FIRE_PASSWORD
import ac.uk.ebi.biostd.itest.common.FIRE_USERNAME
import ac.uk.ebi.biostd.itest.common.SpecificMySQLContainer
import ac.uk.ebi.biostd.itest.wiremock.TestWireMockTransformer
import ac.uk.ebi.biostd.itest.wiremock.TestWireMockTransformer.Companion.newTransformer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.db.MYSQL_SCHEMA
import ebi.ac.uk.db.MYSQL_VERSION
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.utility.DockerImageName.parse
import uk.ac.ebi.fire.client.api.FIRE_OBJECTS_URL
import java.io.File
import java.nio.file.Files
import java.time.Duration.ofSeconds

class ITestListener : TestExecutionListener {
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        mongoSetup()
        mySqlSetup()
        fireSetup()
        appPropertiesSetup()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        mongoContainer.stop()
        mysqlContainer.stop()
        fireApiMock.stop()
    }

    private fun mongoSetup() {
        mongoContainer.start()
        System.setProperty("spring.data.mongodb.uri", mongoContainer.getReplicaSetUrl("biostudies-test"))
        System.setProperty("spring.data.mongodb.database", "biostudies-test")
    }

    private fun mySqlSetup() {
        mysqlContainer.start()
        System.setProperty("spring.datasource.url", mysqlContainer.jdbcUrl)
        System.setProperty("spring.datasource.username", mysqlContainer.username)
        System.setProperty("spring.datasource.password", mysqlContainer.password)
    }

    private fun fireSetup() {
        fireApiMock.stubFor(
            post(WireMock.urlMatching(FIRE_OBJECTS_URL))
                .withBasicAuth(FIRE_USERNAME, FIRE_PASSWORD)
                .willReturn(WireMock.aResponse().withTransformers(TestWireMockTransformer.name))
        )
        fireApiMock.start()

        System.setProperty("app.fire.host", fireApiMock.baseUrl())
        System.setProperty("app.fire.username", FIRE_USERNAME)
        System.setProperty("app.fire.password", FIRE_PASSWORD)
    }

    private fun appPropertiesSetup() {
        System.setProperty("app.submissionPath", nfsSubmissionPath.absolutePath)
        System.setProperty("app.ftpPath", ftpPath.absolutePath)
        System.setProperty("app.fireTempDirPath", fireTempFolder.absolutePath)
        System.setProperty("app.tempDirPath", tempDirPath.absolutePath)
        System.setProperty("app.requestFilesPath", requestFilesPath.absolutePath)
        System.setProperty("app.security.filesDirPath", dropboxPath.absolutePath)
        System.setProperty("app.security.magicDirPath", magicDirPath.absolutePath)
        System.setProperty("app.persistence.enableFire", "${System.getProperty("enableFire").toBoolean()}")
    }

    companion object {
        private val testAppFolder = Files.createTempDirectory("test-app-folder").toFile()
        private val nfsSubmissionPath = testAppFolder.createDirectory("submission")
        private val fireSubmissionPath = testAppFolder.createDirectory("submission-fire")
        private val firePath = testAppFolder.createDirectory("fire-db")

        internal val fireTempFolder = testAppFolder.createDirectory("fire-temp")
        internal val ftpPath = testAppFolder.createDirectory("ftpPath")
        internal val tempDirPath = testAppFolder.createDirectory("tempDirPath")
        internal val tempFolder = testAppFolder.createDirectory("testTempDir")
        internal val requestFilesPath = testAppFolder.createDirectory("requestFilesPath")
        internal val magicDirPath = testAppFolder.createDirectory("magic")
        internal val dropboxPath = testAppFolder.createDirectory("dropbox")

        private val mongoContainer = createMongoContainer()
        private val mysqlContainer = createMysqlContainer()
        private val fireApiMock = createFireApiMock(ftpPath)

        val enableFire get() = System.getProperty("enableFire").toBoolean()
        val submissionPath get() = if (enableFire) fireSubmissionPath else nfsSubmissionPath

        private fun createMongoContainer(): MongoDBContainer =
            MongoDBContainer(parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        private fun createMysqlContainer(): SpecificMySQLContainer =
            SpecificMySQLContainer(MYSQL_VERSION)
                .withCommand("mysqld --character-set-server=$CHARACTER_SET --collation-server=$COLLATION")
                .withInitScript(MYSQL_SCHEMA)
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        private fun createFireApiMock(ftpDir: File): WireMockServer {
            val factor = 5
            val transformer = newTransformer(fireSubmissionPath.toPath(), ftpDir.toPath(), firePath.toPath(), factor)

            return WireMockServer(WireMockConfiguration().dynamicPort().extensions(transformer))
        }

        private fun File.createDirectory(path: String): File {
            val file = resolve(path)
            file.mkdir()

            return file
        }
    }
}
