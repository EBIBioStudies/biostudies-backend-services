package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.config.AppConfig
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.ERROR_SUBMIT
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.SUBMITTED
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.json.toJsonQuote
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
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
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_LENGTH
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import java.net.HttpURLConnection.HTTP_OK

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration
@ExtendWith(TemporaryFolderExtension::class)
internal class PmcSubmissionSubmitterTest(private val tempFolder: TemporaryFolder) {

    private val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
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
                        }.toString()
                    )
                )
                .willReturn(
                    aResponse().withStatus(HTTP_OK).withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(
                            jsonObj {
                                "sessid" to "sessid"
                                "email" to "email"
                                "username" to "username"
                                "secret" to "secret"
                                "fullname" to "fullname"
                                "superuser" to true
                                "allow" to jsonArray("allow")
                                "deny" to jsonArray("deny")
                                "aux" to jsonObj { "orcid" to "orcid" }
                            }.toString()
                        )
                )
        )
        wireMockWebServer.stubFor(
            post(urlEqualTo("/submissions"))
                .withHeader(CONTENT_TYPE, containing(MULTIPART_FORM_DATA))
                .withHeader(ACCEPT, equalTo("$APPLICATION_JSON, $APPLICATION_JSON"))
                .withHeader(SUBMISSION_TYPE, equalTo(APPLICATION_JSON))
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("submission")
                        .withHeader(CONTENT_TYPE, equalTo("$TEXT_PLAIN;charset=UTF-8"))
                        .withHeader(CONTENT_LENGTH, equalTo("225"))
                        .withBody(equalToJson(submissionDoc3.body))
                )
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("files")
                        .withHeader(CONTENT_TYPE, equalTo(TEXT_PLAIN))
                        .withHeader(CONTENT_LENGTH, equalTo("19"))
                )
                .willReturn(
                    aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(body3.toString())
                )
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
                    .withBody("response".toJsonQuote())
            )
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
    @ExtendWith(SpringExtension::class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @SpringBootTest(classes = [AppConfig::class])
    @DirtiesContext
    inner class PmcSubmitTest(
        @Autowired val errorsRepository: ErrorsRepository,
        @Autowired val submissionRepository: SubmissionRepository,
        @Autowired val fileRepository: SubFileRepository,
        @Autowired val pmcTaskExecutor: PmcTaskExecutor,
        @Autowired val resourceLoader: ResourceLoader
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
            runBlocking {
                val resourceFile = resourceLoader.getResource("classpath:$FILE1_NAME").file
                val targetFile = tempFolder.root.resolve(FILE1_NAME)
                resourceFile.copyTo(targetFile)

                val fileObjectId = fileRepository.saveFile(targetFile, submissionDoc3.accno)
                submissionRepository.insertOrExpire(submissionDoc3.copy(files = listOf(fileObjectId)))

                pmcTaskExecutor.run()

                assertThat(errorsRepository.findAll()).isEmpty()
                assertThat(fileRepository.findAll()).hasSize(1)

                val submissions = submissionRepository.findAll()
                assertThat(submissions).hasSize(1)

                val submission = submissions.first()
                assertThat(submission.status).isEqualTo(SUBMITTED)
                assertThat(submission.files).hasSize(1)
            }
        }

        @Test
        fun `when error submit`() {
            runBlocking {
                submissionRepository.insertOrExpire(submissionDoc3)

                pmcTaskExecutor.run()

                assertThat(errorsRepository.findAll()).hasSize(1)
                assertThat(fileRepository.findAll()).isEmpty()

                val submissions = submissionRepository.findAll()
                assertThat(submissions).hasSize(1)

                val submission = submissions.first()
                assertThat(submission.status).isEqualTo(ERROR_SUBMIT)
                assertThat(submission.files).isEmpty()
            }
        }
    }
}
