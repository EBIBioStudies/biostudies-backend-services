package ac.uk.ebi.pmc.process

import ac.uk.ebi.pmc.ERROR_ACCNO
import ac.uk.ebi.pmc.ERROR_SOURCE_FILE
import ac.uk.ebi.pmc.FILE1_CONTENT
import ac.uk.ebi.pmc.FILE1_NAME
import ac.uk.ebi.pmc.FILE1_PATH
import ac.uk.ebi.pmc.FILE2_CONTENT
import ac.uk.ebi.pmc.FILE2_NAME
import ac.uk.ebi.pmc.FILE2_PATH
import ac.uk.ebi.pmc.FILE3_PATH
import ac.uk.ebi.pmc.PmcTaskExecutor
import ac.uk.ebi.pmc.URL_FILE1_FILES_SERVER
import ac.uk.ebi.pmc.URL_FILE2_FILES_SERVER
import ac.uk.ebi.pmc.URL_FILE3_FILES_SERVER
import ac.uk.ebi.pmc.config.AppConfig
import ac.uk.ebi.pmc.config.PersistenceConfig
import ac.uk.ebi.pmc.docSubmission
import ac.uk.ebi.pmc.invalidFileSubmission
import ac.uk.ebi.pmc.persistence.docs.FileDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.ERROR_PROCESS
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.PROCESSED
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.scheduler.properties.PmcMode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.toJsonQuote
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_OK
import java.time.Duration.ofSeconds

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = [PersistenceConfig::class])
@ExtendWith(TemporaryFolderExtension::class)
internal class PmcSubmissionProcessorTest(private val tempFolder: TemporaryFolder) {
    private val mongoContainer: MongoDBContainer =
        MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))
    private val wireMockNotificationServer = WireMockServer(WireMockConfiguration().dynamicPort())
    private val wireMockFilesServer = WireMockServer(WireMockConfiguration().dynamicPort())

    private val submissionFile1 = tempFolder.createFile(FILE1_NAME, FILE1_CONTENT)
    private val submissionFile2 = tempFolder.createFile(FILE2_NAME, FILE2_CONTENT)

    @BeforeAll
    fun beforeAll() {
        setUpMongo()
        setUpWireMockFilesServer()
        setUpNotificationServer()
    }

    private fun setUpNotificationServer() {
        wireMockNotificationServer.stubFor(
            WireMock.post(urlEqualTo("/notifications")).willReturn(
                aResponse().withStatus(HTTP_OK).withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("response".toJsonQuote()),
            ),
        )
        wireMockNotificationServer.start()
        System.setProperty(
            "app.data.notificationsUrl",
            "http://localhost:${wireMockNotificationServer.port()}/notifications",
        )
    }

    private fun setUpWireMockFilesServer() {
        wireMockFilesServer.stubFor(
            get(urlEqualTo(URL_FILE1_FILES_SERVER)).willReturn(
                aResponse().withStatus(HTTP_OK)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody(submissionFile1.readBytes()),
            ),
        )

        wireMockFilesServer.stubFor(
            get(urlEqualTo(URL_FILE2_FILES_SERVER)).willReturn(
                aResponse().withStatus(HTTP_OK)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody(submissionFile2.readBytes()),
            ),
        )

        wireMockFilesServer.stubFor(
            get(urlEqualTo(URL_FILE3_FILES_SERVER)).willReturn(
                aResponse().withStatus(HTTP_BAD_REQUEST),
            ),
        )
        wireMockFilesServer.start()
        System.setProperty("app.data.pmcBaseUrl", "http://localhost:${wireMockFilesServer.port()}")
    }

    @AfterAll
    fun afterAll() {
        mongoContainer.stop()
        wireMockNotificationServer.stop()
        wireMockFilesServer.stop()
    }

    private fun setUpMongo() {
        mongoContainer.start()
        System.setProperty("app.data.mode", "PROCESS")
        System.clearProperty("app.data.sourceFile")
        System.setProperty("app.data.temp", tempFolder.root.path)
        System.setProperty("app.data.mongodbUri", mongoContainer.getReplicaSetUrl("pmc-processor-test"))
        System.setProperty("app.data.mongodbDatabase", "pmc-processor-test")
    }

    @Nested
    @ExtendWith(SpringExtension::class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @SpringBootTest(classes = [AppConfig::class])
    @DirtiesContext
    inner class PmcProcessTest(
        @Autowired val errorsRepository: ErrorsRepository,
        @Autowired val submissionRepository: SubmissionRepository,
        @Autowired val fileRepository: SubFileRepository,
        @Autowired val pmcTaskExecutor: PmcTaskExecutor,
    ) {
        @BeforeEach
        fun cleanRepositories() {
            runBlocking {
                errorsRepository.deleteAll()
                submissionRepository.deleteAll()
                fileRepository.deleteAll()
            }
        }

        @Test
        fun `when success`() {
            runBlocking {
                submissionRepository.insertOrExpire(docSubmission)

                pmcTaskExecutor.run()

                assertThat(errorsRepository.findAll()).isEmpty()
                assertThat(fileRepository.findAll()).hasSize(2)

                val submissions = submissionRepository.findAll()
                assertThat(submissions).hasSize(1)
                assertProcessedSubmission(submissions.first(), fileRepository.findAll())
            }
        }

        @Test
        fun `when error`() {
            runBlocking {
                submissionRepository.insertOrExpire(invalidFileSubmission)

                pmcTaskExecutor.run()

                val errors = errorsRepository.findAll()
                assertThat(errors).hasSize(1)
                assertError(errors.first())

                val submissions = submissionRepository.findAll()
                assertThat(submissions).hasSize(1)
                assertErrorProcessSubmission(submissions.first())

                assertThat(fileRepository.findAll()).isEmpty()
            }
        }

        private fun assertErrorProcessSubmission(errorProcessedSubmission: SubmissionDoc) {
            assertThat(errorProcessedSubmission.status).isEqualTo(ERROR_PROCESS)
            assertThat(errorProcessedSubmission.files).isEmpty()
            assertThat(tempFolder.root.resolve(FILE3_PATH)).doesNotExist()
        }

        private fun assertProcessedSubmission(
            docSubmission: SubmissionDoc,
            files: List<FileDoc>,
        ) {
            assertThat(docSubmission.status).isEqualTo(PROCESSED)
            assertThat(docSubmission.files).hasSize(2)
            assertThat(docSubmission.files).containsAll(files.map { it._id })

            assertThat(File(tempFolder.root.absolutePath + FILE1_PATH)).hasContent(FILE1_CONTENT)
            assertThat(File(tempFolder.root.absolutePath + FILE2_PATH)).hasContent(FILE2_CONTENT)
        }

        private fun assertError(savedError: SubmissionErrorDoc) {
            assertThat(savedError.accNo).isEqualTo(ERROR_ACCNO)
            assertThat(savedError.sourceFile).isEqualTo(ERROR_SOURCE_FILE)
            assertThat(savedError.mode).isEqualTo(PmcMode.PROCESS)
            assertThat(savedError.submissionText).isEqualTo(invalidFileSubmission.body)
        }
    }
}
