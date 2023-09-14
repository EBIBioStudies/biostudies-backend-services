package ac.uk.ebi.biostd.itest.itest

import ac.uk.ebi.biostd.itest.common.CHARACTER_SET
import ac.uk.ebi.biostd.itest.common.COLLATION
import ac.uk.ebi.biostd.itest.common.FIRE_PASSWORD
import ac.uk.ebi.biostd.itest.common.FIRE_USERNAME
import ac.uk.ebi.biostd.itest.common.SpecificMySQLContainer
import ac.uk.ebi.biostd.itest.wiremock.TestWireMockTransformer
import ac.uk.ebi.biostd.itest.wiremock.TestWireMockTransformer.Companion.newTransformer
import com.adobe.testing.s3mock.testcontainers.S3MockContainer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.db.MYSQL_SCHEMA
import ebi.ac.uk.db.MYSQL_VERSION
import ebi.ac.uk.extended.model.StorageMode
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.utility.DockerImageName.parse
import java.io.File
import java.nio.file.Files
import java.time.Duration.ofSeconds

class ITestListener : TestExecutionListener {
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        mongoSetup()
        mySqlSetup()
        fireSetup()
        ftpSetup()
        appPropertiesSetup()
        doiSetup()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        mongoContainer.stop()
        mysqlContainer.stop()
        fireServer.stop()
        ftpServer.stop()
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

    private fun ftpSetup() {
        ftpServer.start()

        System.setProperty("app.security.filesProperties.ftpUser", FTP_USER)
        System.setProperty("app.security.filesProperties.ftpPassword", FTP_PASSWORD)
        System.setProperty("app.security.filesProperties.ftpUrl", ftpServer.getUrl())
        System.setProperty("app.security.filesProperties.ftpPort", ftpServer.ftpPort.toString())
    }

    private fun fireSetup() {
        s3Container.start()
        System.setProperty("app.fire.s3.accessKey", AWS_ACCESS_KEY)
        System.setProperty("app.fire.s3.secretKey", AWS_SECRET_KEY)
        System.setProperty("app.fire.s3.region", AWS_REGION)
        System.setProperty("app.fire.s3.endpoint", s3Container.httpEndpoint)
        System.setProperty("app.fire.s3.bucket", DEFAULT_BUCKET)

        fireServer.stubFor(
            post(WireMock.urlMatching("/objects"))
                .withBasicAuth(FIRE_USERNAME, FIRE_PASSWORD)
                .willReturn(WireMock.aResponse().withTransformers(TestWireMockTransformer.name))
        )
        fireServer.start()
        System.setProperty("app.fire.host", fireServer.baseUrl())
        System.setProperty("app.fire.username", FIRE_USERNAME)
        System.setProperty("app.fire.password", FIRE_PASSWORD)
    }

    private fun appPropertiesSetup() {
        System.setProperty("app.submissionPath", nfsSubmissionPath.absolutePath)
        System.setProperty("app.ftpPath", nfsFtpPath.absolutePath)
        System.setProperty("app.fireTempDirPath", fireTempFolder.absolutePath)
        System.setProperty("app.tempDirPath", tempDirPath.absolutePath)
        System.setProperty("app.requestFilesPath", requestFilesPath.absolutePath)
        System.setProperty("app.security.filesProperties.filesDirPath", dropboxPath.absolutePath)
        System.setProperty("app.security.filesProperties.magicDirPath", magicDirPath.absolutePath)
        System.setProperty("app.persistence.concurrency", PERSISTENCE_CONCURRENCY)
        System.setProperty("app.persistence.enableFire", "${System.getProperty("enableFire").toBoolean()}")
    }

    private fun doiSetup() {
        doiServer.start()
        System.setProperty("app.doi.endpoint", "${doiServer.baseUrl()}/deposit")
        System.setProperty("app.doi.uiUrl", "https://www.ebi.ac.uk/biostudies/")
        System.setProperty("app.doi.user", "a-user")
        System.setProperty("app.doi.password", "a-password")
    }

    companion object {
        private val testAppFolder = Files.createTempDirectory("test-app-folder").toFile()
        private const val DEFAULT_BUCKET = "bio-fire-bucket"
        private const val AWS_ACCESS_KEY = "anyKey"
        private const val AWS_SECRET_KEY = "anySecret"
        private const val AWS_REGION = "anyRegion"
        private const val FAIL_FACTOR_ENV = "ITEST_FAIL_FACTOR"
        private const val PERSISTENCE_CONCURRENCY = "10"

        private const val FTP_USER = "ftpUser"
        private const val FTP_PASSWORD = "ftpPassword"

        internal const val FIXED_DELAY_ENV = "ITEST_FIXED_DELAY"
        internal val nfsSubmissionPath = testAppFolder.createDirectory("submission")
        internal val fireSubmissionPath = testAppFolder.createDirectory("submission-fire")
        private val firePath = testAppFolder.createDirectory("fire-db")

        internal val fireTempFolder = testAppFolder.createDirectory("fire-temp")
        internal val nfsFtpPath = testAppFolder.createDirectory("ftpPath")
        internal val fireFtpPath = testAppFolder.createDirectory("fire-ftpPath")

        internal val tempDirPath = testAppFolder.createDirectory("tempDirPath")
        internal val tempFolder = testAppFolder.createDirectory("testTempDir")
        internal val requestFilesPath = testAppFolder.createDirectory("requestFilesPath")
        internal val magicDirPath = testAppFolder.createDirectory("magic")
        internal val dropboxPath = testAppFolder.createDirectory("dropbox")

        private val fireServer: WireMockServer by lazy { createFireApiMock() }
        private val doiServer: WireMockServer by lazy { createDoiApiMock() }
        private val ftpServer = createFtpServer()

        private val mongoContainer = createMongoContainer()
        private val mysqlContainer = createMysqlContainer()
        private val s3Container = createMockS3Container()

        val enableFire get() = System.getProperty("enableFire").toBoolean()
        val storageMode get() = if (enableFire) StorageMode.FIRE else StorageMode.NFS
        val submissionPath get() = if (enableFire) fireSubmissionPath else nfsSubmissionPath
        val ftpPath get() = if (enableFire) fireFtpPath else nfsFtpPath

        private fun createMongoContainer(): MongoDBContainer =
            MongoDBContainer(parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        private fun createMysqlContainer(): SpecificMySQLContainer =
            SpecificMySQLContainer(MYSQL_VERSION)
                .withCommand("mysqld --character-set-server=$CHARACTER_SET --collation-server=$COLLATION")
                .withInitScript(MYSQL_SCHEMA)
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        private fun createMockS3Container(): S3MockContainer = S3MockContainer("latest")
            .withInitialBuckets(DEFAULT_BUCKET)

        private fun createFtpServer(): FtpServer {
            return FtpServer.createServer(
                FtpConfig(
                    sslConfig = SslConfig(File(this::class.java.getResource("/mykeystore.jks").toURI()), "123456"),
                    userName = FTP_USER,
                    password = FTP_PASSWORD
                )
            )
        }

        private fun createDoiApiMock(): WireMockServer {
            val doiServer = WireMockServer(WireMockConfiguration().dynamicPort())
            doiServer.stubFor(post("/deposit").willReturn(okForJson("ok")))

            return doiServer
        }

        private fun createFireApiMock(): WireMockServer {
            val factor = System.getenv(FAIL_FACTOR_ENV)?.toInt()
            val delay = System.getenv(FIXED_DELAY_ENV)?.toLong() ?: 0L
            val transformer = newTransformer(
                subFolder = fireSubmissionPath.toPath(),
                ftpFolder = fireFtpPath.toPath(),
                dbFolder = firePath.toPath(),
                failFactor = factor,
                fixedDelay = delay,
                httpEndpoint = s3Container.httpEndpoint,
                defaultBucket = DEFAULT_BUCKET
            )
            return WireMockServer(WireMockConfiguration().dynamicPort().extensions(transformer))
        }

        private fun File.createDirectory(path: String): File {
            val file = resolve(path)
            file.mkdir()
            return file
        }
    }
}
