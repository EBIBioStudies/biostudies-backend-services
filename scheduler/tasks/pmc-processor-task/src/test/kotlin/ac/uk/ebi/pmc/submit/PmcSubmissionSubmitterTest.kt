package ac.uk.ebi.pmc.submit

import ac.uk.ebi.pmc.FILE1_CONTENT
import ac.uk.ebi.pmc.FILE1_NAME
import ac.uk.ebi.pmc.PmcTaskExecutor
import ac.uk.ebi.pmc.config.AppConfig
import ac.uk.ebi.pmc.persistence.docs.SubFileDocument
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.ERROR_SUBMIT
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.SUBMITTED
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileDocRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionDocRepository
import ac.uk.ebi.pmc.processedSubmission
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.json.toJsonQuote
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.SUBFORMAT
import ebi.ac.uk.model.constants.SUBMISSIONS
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
import org.springframework.http.HttpHeaders.CONTENT_LENGTH
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.utility.DockerImageName
import java.net.HttpURLConnection.HTTP_OK
import java.time.Duration.ofSeconds

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration
internal class PmcSubmissionSubmitterTest {
    private val mongoContainer: MongoDBContainer =
        MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))
    private val wireMockNotificationServer = WireMockServer(WireMockConfiguration().dynamicPort())
    private val wireMockWebServer = WireMockServer(WireMockConfiguration().dynamicPort())

    @BeforeAll
    fun beforeAll() {
        setUpMongo()
        setUpWireMockNotificationServer()
        setUpWireMockWebServer()
    }

    private fun setUpWireMockWebServer() {
        wireMockWebServer.stubFor(
            post(urlEqualTo("/auth/login"))
                .withRequestBody(
                    equalToJson(
                        jsonObj {
                            "login" to "admin_user@ebi.ac.uk"
                            "password" to "123456"
                        }.toString(),
                    ),
                ).willReturn(
                    aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(
                            jsonObj {
                                "sessid" to "sessid"
                                "email" to "email"
                                "username" to "username"
                                "secret" to "secret"
                                "fullname" to "fullname"
                                "superuser" to true
                                "orcid" to "orcid"
                                "allow" to jsonArray("allow")
                                "deny" to jsonArray("deny")
                                "uploadType" to "nfs"
                            }.toString(),
                        ),
                ),
        )
        wireMockWebServer.stubFor(
            post(urlEqualTo("/submissions/async/multiple"))
                .withHeader(CONTENT_TYPE, containing(MULTIPART_FORM_DATA))
                .withMultipartRequestBody(
                    aMultipart()
                        .withName(SUBMISSIONS)
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
                        .withBody(
                            equalToJson(
                                jsonObj {
                                    processedSubmission.accNo to processedSubmission.body
                                }.toString(),
                            ),
                        ),
                ).withMultipartRequestBody(
                    aMultipart()
                        .withName(SUBFORMAT)
                        .withBody(equalTo("json")),
                ).withMultipartRequestBody(
                    aMultipart()
                        .withName(processedSubmission.accNo)
                        .withHeader(CONTENT_TYPE, equalTo(TEXT_PLAIN))
                        .withHeader(CONTENT_LENGTH, equalTo("19"))
                        .withBody(equalTo(FILE1_CONTENT)),
                ).willReturn(
                    aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(
                            jsonArray(
                                jsonObj {
                                    "accNo" to "S-EPMC1234567"
                                    "version" to 3
                                },
                            ).toString(),
                        ),
                ),
        )
        wireMockWebServer.start()
        System.setProperty("app.data.bioStudiesUrl", "http://localhost:${wireMockWebServer.port()}")
    }

    private fun setUpWireMockNotificationServer() {
        wireMockNotificationServer.stubFor(
            post(urlEqualTo("/")).willReturn(
                aResponse()
                    .withStatus(HTTP_OK)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("response".toJsonQuote()),
            ),
        )
        wireMockNotificationServer.start()
        System.setProperty("app.data.notificationsUrl", "http://localhost:${wireMockNotificationServer.port()}")
    }

    private fun setUpMongo() {
        mongoContainer.start()
        System.setProperty("app.data.mode", "SUBMIT")
        System.setProperty("app.data.mongodbUri", mongoContainer.getReplicaSetUrl("pmc-submitter-test"))
        System.setProperty("app.data.mongodbDatabase", "pmc-submitter-test")
        System.setProperty("app.data.bioStudiesUser", "admin_user@ebi.ac.uk")
        System.setProperty("app.data.bioStudiesPassword", "123456")
    }

    @AfterAll
    fun afterAll() {
        mongoContainer.stop()
        wireMockNotificationServer.stop()
    }

    @Nested
    @ExtendWith(SpringExtension::class, TemporaryFolderExtension::class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @SpringBootTest(classes = [AppConfig::class])
    @DirtiesContext
    inner class PmcSubmitTest(
        @Autowired val errorsRepository: ErrorsRepository,
        @Autowired val submissionRepository: SubmissionDocRepository,
        @Autowired val fileRepository: SubFileDocRepository,
        @Autowired val pmcTaskExecutor: PmcTaskExecutor,
        private val tempFolder: TemporaryFolder,
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
        fun `when success submit`() {
            runTest {
                val targetFile = tempFolder.createFile(FILE1_NAME, FILE1_CONTENT)
                val fileDoc =
                    SubFileDocument(
                        accNo = processedSubmission.accNo,
                        name = targetFile.name,
                        path = targetFile.absolutePath,
                    )
                val fileObjectId = fileRepository.save(fileDoc).id
                submissionRepository.save(processedSubmission.copy(files = listOf(fileObjectId)))

                pmcTaskExecutor.run()

                assertThat(errorsRepository.findAll().toList()).isEmpty()
                assertThat(fileRepository.findAll().toList()).hasSize(1)

                val submissions = submissionRepository.findAll().toList()
                assertThat(submissions).hasSize(1)

                val submission = submissions.first()
                assertThat(submission.status).isEqualTo(SUBMITTED)
                assertThat(submission.version).isEqualTo(3)
                assertThat(submission.files).hasSize(1)
            }
        }

        @Test
        fun `when error submit`() {
            runTest {
                submissionRepository.save(processedSubmission)

                pmcTaskExecutor.run()

                assertThat(errorsRepository.findAll().toList()).hasSize(1)
                assertThat(fileRepository.findAll().toList()).isEmpty()

                val submissions = submissionRepository.findAll().toList()
                assertThat(submissions).hasSize(1)

                val submission = submissions.first()
                assertThat(submission.status).isEqualTo(ERROR_SUBMIT)
                assertThat(submission.files).isEmpty()
            }
        }
    }
}
