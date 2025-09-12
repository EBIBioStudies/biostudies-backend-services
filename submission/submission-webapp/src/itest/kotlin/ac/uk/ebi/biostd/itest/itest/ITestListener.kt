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
import ebi.ac.uk.db.RABBIT_VERSION
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.ftp.FtpConfig
import ebi.ac.uk.ftp.FtpServer
import ebi.ac.uk.ftp.SslConfig
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.utility.DockerImageName.parse
import java.io.File
import java.nio.file.Files
import java.time.Duration.ofSeconds

class ITestListener : TestExecutionListener {
    private val properties = ConfigurationPropertiesHolder()

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        mongoSetup()
        mySqlSetup()
        rabbitSetup()
        fireSetup()
        securitySetup()
        doiSetup()
        persistenceSetup()
        submissionTaskSetup()
        clusterSetup()

        properties.writeProperties()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        mongoContainer.stop()
        mysqlContainer.stop()
        rabbitMQContainer.stop()
        fireServer.stop()
        ftpInServer.stop()
    }

    private fun mongoSetup() {
        mongoContainer.start()
        properties.addProperty(
            "spring.data.mongodb.uri",
            mongoContainer.getReplicaSetUrl("biostudies-test"),
        )
        properties.addProperty("spring.data.mongodb.database", "biostudies-test")
    }

    private fun mySqlSetup() {
        mysqlContainer.start()
        properties.addProperty("spring.datasource.url", mysqlContainer.jdbcUrl)
        properties.addProperty("spring.datasource.username", mysqlContainer.username)
        properties.addProperty("spring.datasource.password", mysqlContainer.password)
    }

    private fun rabbitSetup() {
        rabbitMQContainer.start()
        properties.addProperty("spring.rabbitmq.host", rabbitMQContainer.host)
        properties.addProperty("spring.rabbitmq.port", rabbitMQContainer.amqpPort)
        properties.addProperty("spring.rabbitmq.username", rabbitMQContainer.adminUsername)
        properties.addProperty("spring.rabbitmq.password", rabbitMQContainer.adminPassword)
    }

    private fun securitySetup() {
        val securityProperties = "app.security"
        properties.addProperty("$securityProperties.environment", ENVIRONMENT)

        ftpInServer.start()
        ftpOutServer.start()

        val userFilesProperties = "$securityProperties.filesProperties"
        properties.addProperty("$userFilesProperties.filesDirPath", dropboxPath.absolutePath)
        properties.addProperty("$userFilesProperties.magicDirPath", magicDirPath.absolutePath)
        properties.addProperty("$userFilesProperties.userFtpDirPath", ftpInServer.fileSystemDirectory.absolutePath)
        properties.addProperty("$userFilesProperties.userFtpRootPath", ".test")
        Files.createDirectory(ftpInServer.fileSystemDirectory.resolve(FTP_ROOT_PATH).toPath())

        val ftpProperties = "$userFilesProperties.ftpIn"
        properties.addProperty("$ftpProperties.ftpUser", FTP_USER)
        properties.addProperty("$ftpProperties.ftpPassword", FTP_PASSWORD)
        properties.addProperty("$ftpProperties.ftpUrl", ftpInServer.getUrl())
        properties.addProperty("$ftpProperties.ftpPort", ftpInServer.ftpPort.toString())
        properties.addProperty("$ftpProperties.defaultTimeout", FTP_DEFAULT_TIMEOUT)
        properties.addProperty("$ftpProperties.connectionTimeout", FTP_DEFAULT_TIMEOUT)
        properties.addProperty("$ftpProperties.retry.maxAttempts", 2)
        properties.addProperty("$ftpProperties.retry.initialInterval", 100)
        properties.addProperty("$ftpProperties.retry.multiplier", 2)
        properties.addProperty("$ftpProperties.retry.maxInterval", 500)

        // Submission FTP
        val subFtpProperties = "$userFilesProperties.ftpOut"
        properties.addProperty("$subFtpProperties.ftpUser", "anonymous")
        properties.addProperty("$subFtpProperties.ftpPassword", "")
        properties.addProperty("$subFtpProperties.ftpUrl", ftpOutServer.getUrl())
        properties.addProperty("$subFtpProperties.ftpPort", ftpOutServer.ftpPort.toString())
        properties.addProperty("$subFtpProperties.defaultTimeout", FTP_DEFAULT_TIMEOUT)
        properties.addProperty("$subFtpProperties.connectionTimeout", FTP_DEFAULT_TIMEOUT)
        properties.addProperty("$subFtpProperties.retry.maxAttempts", 2)
        properties.addProperty("$subFtpProperties.retry.initialInterval", 100)
        properties.addProperty("$subFtpProperties.retry.multiplier", 2)
        properties.addProperty("$subFtpProperties.retry.maxInterval", 500)
    }

    private fun fireSetup() {
        s3Container.start()
        properties.addProperty("app.fire.s3.accessKey", AWS_ACCESS_KEY)
        properties.addProperty("app.fire.s3.secretKey", AWS_SECRET_KEY)
        properties.addProperty("app.fire.s3.region", AWS_REGION)
        properties.addProperty("app.fire.s3.endpoint", s3Container.httpEndpoint)
        properties.addProperty("app.fire.s3.bucket", DEFAULT_BUCKET)

        fireServer.stubFor(
            post(WireMock.urlMatching("/objects"))
                .withBasicAuth(FIRE_USERNAME, FIRE_PASSWORD)
                .willReturn(WireMock.aResponse().withTransformers(TestWireMockTransformer.NAME)),
        )
        fireServer.start()
        properties.addProperty("app.fire.host", fireServer.baseUrl())
        properties.addProperty("app.fire.username", FIRE_USERNAME)
        properties.addProperty("app.fire.password", FIRE_PASSWORD)
        properties.addProperty("app.fire.tempDirPath", fireTempFolder.absolutePath)
    }

    private fun persistenceSetup() {
        properties.addProperty("app.persistence.concurrency", PERSISTENCE_CONCURRENCY)
        properties.addProperty(
            "app.persistence.enableFire",
            "${System.getProperty("enableFire").toBoolean()}",
        )
        properties.addProperty(
            "app.persistence.includeSecretKey",
            "${System.getProperty("includeSecretKey").toBoolean()}",
        )
        properties.addProperty("app.persistence.nfsReleaseMode", System.getProperty("nfsReleaseMode"))
        properties.addProperty("app.persistence.pageTabFallbackPath", pageTabFallbackPath.absolutePath)
        properties.addProperty("app.persistence.privateSubmissionsPath", privateNfsSubmissionPath.absolutePath)
        properties.addProperty("app.persistence.publicSubmissionsPath", publicNfsSubmissionPath.absolutePath)
        properties.addProperty("app.persistence.privateSubmissionFtpOutPath", PRIVATE_SUBMISSION_PATH)
        properties.addProperty("app.persistence.publicSubmissionFtpOutPath", PUBLIC_SUBMISSION_PATH)
        properties.addProperty("app.persistence.tempDirPath", tempDirPath.absolutePath)
        properties.addProperty("app.persistence.requestFilesPath", requestFilesPath.absolutePath)
    }

    private fun doiSetup() {
        doiServer.start()
        properties.addProperty("app.doi.endpoint", "${doiServer.baseUrl()}/deposit")
        properties.addProperty("app.doi.uiUrl", "https://www.ebi.ac.uk/biostudies/")
        properties.addProperty("app.doi.email", "biostudies@ebi.ac.uk")
        properties.addProperty("app.doi.user", "a-user")
        properties.addProperty("app.doi.password", "a-password")
    }

    private fun submissionTaskSetup() {
        val javaLocation = findResource("java")?.absolutePath.orEmpty()
        val configFile = findResource("application.yml")?.absolutePath.orEmpty()
        val jarLocation = findResource("submission-task-1.0.0.jar")?.absolutePath.orEmpty()

        properties.addProperty("app.submissionTask.enabled", enableTask)
        properties.addProperty("app.submissionTask.jarLocation", jarLocation)
        properties.addProperty("app.submissionTask.javaLocation", javaLocation)
        properties.addProperty("app.submissionTask.javaMemoryAllocation", 1)
        properties.addProperty("app.submissionTask.tmpFilesDirPath", tempDirPath.absolutePath)
        properties.addProperty("app.submissionTask.singleJobMode", true)
        properties.addProperty("app.submissionTask.configFileLocation", configFile)
        properties.addProperty("app.submissionTask.taskMemoryMgb", 4096)
        properties.addProperty("app.submissionTask.taskCores", 2)
        properties.addProperty("app.submissionTask.taskMinutes", 60)
    }

    private fun clusterSetup() {
        properties.addProperty("app.cluster.enabled", false)
        properties.addProperty("app.cluster.user", "test-user")
        properties.addProperty("app.cluster.key", "test-key")
        properties.addProperty("app.cluster.lsfServer", "test-server")
        properties.addProperty("app.cluster.slurmServer", "test-server")
        properties.addProperty("app.cluster.default", "LSF")
        properties.addProperty("app.cluster.logsPath", clusterLogsPath.absolutePath)
        properties.addProperty("app.cluster.wrapperPath", clusterWrapperPath.absolutePath)
    }

    private fun findResource(resource: String): File? =
        this::class.java
            .getResource("/$resource")
            ?.toURI()
            ?.let { File(it) }

    companion object {
        private const val ENVIRONMENT = "TEST"
        private const val FTP_ROOT_PATH = ".test"
        private const val FTP_DEFAULT_TIMEOUT = 3000L

        private val testAppFolder = Files.createTempDirectory("test-app-folder").toFile()
        private val submissionsFtp = Files.createTempDirectory("submissions-ftp").toFile()
        private val userFilesFtp = Files.createTempDirectory("users-dir").toFile()

        private const val DEFAULT_BUCKET = "bio-fire-bucket"
        private const val AWS_ACCESS_KEY = "anyKey"
        private const val AWS_SECRET_KEY = "anySecret"
        private const val AWS_REGION = "anyRegion"
        private const val FAIL_FACTOR_ENV = "ITEST_FAIL_FACTOR"
        private const val PERSISTENCE_CONCURRENCY = "10"

        private const val FTP_USER = "ftpUser"
        private const val FTP_PASSWORD = "ftpPassword"

        internal const val FIXED_DELAY_ENV = "ITEST_FIXED_DELAY"
        internal const val PRIVATE_SUBMISSION_PATH = ".private"

        internal val pageTabFallbackPath = testAppFolder.createDirectory("pagetab-fallback")
        internal val privateNfsSubmissionPath = submissionsFtp.createDirectory(PRIVATE_SUBMISSION_PATH)
        internal val fireSubmissionPath = testAppFolder.createDirectory("submission-fire")
        internal val firePath = testAppFolder.createDirectory("fire-db")

        internal val fireTempFolder = testAppFolder.createDirectory("fire-temp")
        internal const val PUBLIC_SUBMISSION_PATH = ""
        internal val publicNfsSubmissionPath = submissionsFtp.resolve(PUBLIC_SUBMISSION_PATH)
        internal val fireFtpPath = testAppFolder.createDirectory("fire-ftpPath")

        internal val tempDirPath = testAppFolder.createDirectory("tempDirPath")
        internal val tempFolder = testAppFolder.createDirectory("testTempDir")
        internal val requestFilesPath = testAppFolder.createDirectory("requestFilesPath")
        internal val magicDirPath = testAppFolder.createDirectory("magic")
        internal val dropboxPath = testAppFolder.createDirectory("dropbox")
        internal val clusterLogsPath = testAppFolder.createDirectory("cluster-logs")
        internal val clusterWrapperPath = testAppFolder.createDirectory("cluster-wrapper")

        private val fireServer: WireMockServer by lazy { createFireApiMock() }
        private val doiServer: WireMockServer by lazy { createDoiApiMock() }
        private val ftpInServer = createFtpServer(FTP_USER, FTP_PASSWORD, userFilesFtp)
        private val ftpOutServer = createFtpServer("anonymous", "", submissionsFtp)

        private val mongoContainer = createMongoContainer()
        private val mysqlContainer = createMysqlContainer()
        private val s3Container = createMockS3Container()
        private val rabbitMQContainer = createRabbitMqContainer()

        val enableFire get() = System.getProperty("enableFire").toBoolean()
        val enableTask get() = System.getProperty("enableTaskMode").toBoolean()
        val storageMode get() = if (enableFire) StorageMode.FIRE else StorageMode.NFS
        val submissionPath get() = if (enableFire) fireSubmissionPath else privateNfsSubmissionPath
        val ftpPath get() = if (enableFire) fireFtpPath else publicNfsSubmissionPath

        private fun createMongoContainer(): MongoDBContainer =
            MongoDBContainer(parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        private fun createRabbitMqContainer(): RabbitMQContainer = RabbitMQContainer(parse(RABBIT_VERSION))

        private fun createMysqlContainer(): SpecificMySQLContainer =
            SpecificMySQLContainer(MYSQL_VERSION)
                .withCommand("mysqld --character-set-server=$CHARACTER_SET --collation-server=$COLLATION")
                .withInitScript(MYSQL_SCHEMA)
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        private fun createMockS3Container(): S3MockContainer =
            S3MockContainer("latest")
                .withInitialBuckets(DEFAULT_BUCKET)

        private fun createFtpServer(
            user: String,
            pasword: String,
            ftpDirectory: File,
        ): FtpServer =
            FtpServer.createServer(
                FtpConfig(
                    sslConfig = SslConfig(File(this::class.java.getResource("/mykeystore.jks").toURI()), "123456"),
                    userName = user,
                    password = pasword,
                    path = ftpDirectory.toPath(),
                ),
            )

        private fun createDoiApiMock(): WireMockServer {
            val doiServer = WireMockServer(WireMockConfiguration().dynamicPort())
            doiServer.stubFor(post("/deposit").willReturn(okForJson("ok")))

            return doiServer
        }

        private fun createFireApiMock(): WireMockServer {
            val factor = System.getenv(FAIL_FACTOR_ENV)?.toInt()
            val delay = System.getenv(FIXED_DELAY_ENV)?.toLong() ?: 0L
            val transformer =
                newTransformer(
                    subFolder = fireSubmissionPath.toPath(),
                    ftpFolder = fireFtpPath.toPath(),
                    dbFolder = firePath.toPath(),
                    failFactor = factor,
                    fixedDelay = delay,
                    httpEndpoint = s3Container.httpEndpoint,
                    defaultBucket = DEFAULT_BUCKET,
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
