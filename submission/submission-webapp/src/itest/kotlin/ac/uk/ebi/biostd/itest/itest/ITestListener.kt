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

    private val propertyHolder = PropertyHolder()

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        mongoSetup()
        mySqlSetup()
        fireSetup()
        ftpSetup()
        doiSetup()
        appPropertiesSetup()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        mongoContainer.stop()
        mysqlContainer.stop()
        fireServer.stop()
        ftpServer.stop()
    }

    private fun mongoSetup() {
        mongoContainer.start()
        propertyHolder.addProperty("spring.data.mongodb.uri", mongoContainer.getReplicaSetUrl("biostudies-test"))
        propertyHolder.addProperty("spring.data.mongodb.database", "biostudies-test")
    }

    private fun mySqlSetup() {
        mysqlContainer.start()
        propertyHolder.addProperty("spring.datasource.url", mysqlContainer.jdbcUrl)
        propertyHolder.addProperty("spring.datasource.username", mysqlContainer.username)
        propertyHolder.addProperty("spring.datasource.password", mysqlContainer.password)
    }

    private fun ftpSetup() {
        ftpServer.start()

        propertyHolder.addProperty("app.security.filesProperties.ftpUser", FTP_USER)
        propertyHolder.addProperty("app.security.filesProperties.ftpPassword", FTP_PASSWORD)
        propertyHolder.addProperty("app.security.filesProperties.ftpUrl", ftpServer.getUrl())
        propertyHolder.addProperty("app.security.filesProperties.ftpPort", ftpServer.ftpPort.toString())
        propertyHolder.addProperty(
            "app.security.filesProperties.ftpDirPath",
            ftpServer.fileSystemDirectory.absolutePath
        )
    }

    private fun fireSetup() {
        s3Container.start()
        propertyHolder.addProperty("app.fire.s3.accessKey", AWS_ACCESS_KEY)
        propertyHolder.addProperty("app.fire.s3.secretKey", AWS_SECRET_KEY)
        propertyHolder.addProperty("app.fire.s3.region", AWS_REGION)
        propertyHolder.addProperty("app.fire.s3.endpoint", s3Container.httpEndpoint)
        propertyHolder.addProperty("app.fire.s3.bucket", DEFAULT_BUCKET)

        fireServer.stubFor(
            post(WireMock.urlMatching("/objects"))
                .withBasicAuth(FIRE_USERNAME, FIRE_PASSWORD)
                .willReturn(WireMock.aResponse().withTransformers(TestWireMockTransformer.name))
        )
        fireServer.start()
        propertyHolder.addProperty("app.fire.host", fireServer.baseUrl())
        propertyHolder.addProperty("app.fire.username", FIRE_USERNAME)
        propertyHolder.addProperty("app.fire.password", FIRE_PASSWORD)
    }

    private fun appPropertiesSetup() {
        propertyHolder.addProperty("app.submissionPath", nfsSubmissionPath.absolutePath)
        propertyHolder.addProperty("app.ftpPath", nfsFtpPath.absolutePath)
        propertyHolder.addProperty("app.fireTempDirPath", fireTempFolder.absolutePath)
        propertyHolder.addProperty("app.tempDirPath", tempDirPath.absolutePath)
        propertyHolder.addProperty("app.requestFilesPath", requestFilesPath.absolutePath)
        propertyHolder.addProperty("app.security.filesProperties.filesDirPath", dropboxPath.absolutePath)
        propertyHolder.addProperty("app.security.filesProperties.magicDirPath", magicDirPath.absolutePath)
        propertyHolder.addProperty("app.persistence.concurrency", PERSISTENCE_CONCURRENCY)
        propertyHolder.addProperty("app.persistence.enableFire", "${System.getProperty("enableFire").toBoolean()}")
        propertyHolder.writeProperties()
    }

    private fun doiSetup() {
        doiServer.start()
        propertyHolder.addProperty("app.doi.endpoint", "${doiServer.baseUrl()}/deposit")
        propertyHolder.addProperty("app.doi.uiUrl", "https://www.ebi.ac.uk/biostudies/")
        propertyHolder.addProperty("app.doi.user", "a-user")
        propertyHolder.addProperty("app.doi.password", "a-password")
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
