package ac.uk.ebi.pmc

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.pmc.config.AppConfig
import ac.uk.ebi.pmc.persistence.docs.InputFileDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.LOADED
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.scheduler.properties.PmcMode.LOAD
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.toJsonQuote
import ebi.ac.uk.io.ext.gZipTo
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.util.collections.second
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
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import java.net.HttpURLConnection.HTTP_OK

@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration
@ExtendWith(TemporaryFolderExtension::class)
internal class PmcFileLoaderTest(private val tempFolder: TemporaryFolder) {

    private val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
    private val wireMockServer = WireMockServer(WireMockConfiguration().dynamicPort())

    @BeforeAll
    fun beforeAll() {
        setUpMongo()
        setupWireMock()
    }

    @AfterAll
    fun afterAll() {
        mongoContainer.stop()
        wireMockServer.stop()
    }

    private fun setUpMongo() {
        mongoContainer.start()
        System.setProperty("app.data.mode", "LOAD")
        System.setProperty("app.data.loadFolder", tempFolder.root.path)
        System.setProperty("app.data.mongodbUri", mongoContainer.getReplicaSetUrl("pmc-loader-test"))
        System.setProperty("app.data.mongodbDatabase", "pmc-loader-test")
    }

    private fun setupWireMock() {
        wireMockServer.stubFor(
            post(urlPathMatching("/")).willReturn(
                aResponse()
                    .withStatus(HTTP_OK)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .withBody("response".toJsonQuote())
            )
        )
        wireMockServer.start()
        System.setProperty("app.data.notificationsUrl", "http://localhost:${wireMockServer.port()}")
    }

    companion object {
        private const val PMC_EXPORT_FILE = "fileLoadTest.txt"
        private const val PMC_EXPORT_FILE_GZIP = "$PMC_EXPORT_FILE.gz"
    }

    @Nested
    @ExtendWith(SpringExtension::class)
    @TestInstance(Lifecycle.PER_CLASS)
    @SpringBootTest(classes = [AppConfig::class])
    @DirtiesContext
    inner class PmcLoadTest(
        @Autowired val errorsRepository: ErrorsRepository,
        @Autowired val submissionRepository: SubmissionRepository,
        @Autowired val serializationService: SerializationService,
        @Autowired val inputFileRepository: InputFileRepository,
        @Autowired val pmcTaskExecutor: PmcTaskExecutor,
        @Autowired val resourceLoader: ResourceLoader
    ) {
        private val gzipFilePath = tempFolder.root.resolve(PMC_EXPORT_FILE_GZIP).path

        @BeforeEach
        fun beforeEach() {
            val pmcSubmissionsFile = resourceLoader.getResource("classpath:$PMC_EXPORT_FILE").file
            pmcSubmissionsFile.gZipTo(tempFolder.root.resolve(PMC_EXPORT_FILE_GZIP))
        }

        @Test
        fun pmcLoad() {
            runBlocking {
                pmcTaskExecutor.run()

                val errors = errorsRepository.findAll()
                assertThat(errors).hasSize(1)
                assertError(errors.first())

                val submissions = submissionRepository.findAll()
                assertThat(submissions).hasSize(1)
                assertSubmission(submissions.first())

                val docFiles = inputFileRepository.findAll()
                assertThat(docFiles).hasSize(1)
                assertThatDocFile(docFiles.first())

                assertThat(tempFolder.root.resolve(PMC_EXPORT_FILE_GZIP)).doesNotExist()
                assertThat(tempFolder.root.resolve("processed/$PMC_EXPORT_FILE_GZIP")).exists()
            }
        }

        private fun assertThatDocFile(docFile: InputFileDoc) {
            assertThat(docFile.name).isEqualTo(gzipFilePath)
            assertThat(docFile.loaded).isNotNull()
        }

        private fun assertError(savedError: SubmissionErrorDoc) {
            assertThat(savedError.accNo).isNull()
            assertThat(savedError.sourceFile).isEqualTo(gzipFilePath)
            assertThat(savedError.mode).isEqualTo(LOAD)
            assertThat(savedError.submissionText).isEqualTo(SUB_ERROR_TEXT)
            assertThat(savedError.uploaded).isNotEqualTo(processedSubmission.updated)
        }

        private fun assertSubmission(submission: SubmissionDoc) {
            assertThat(submission.accno).isEqualTo(ACC_NO)
            assertThat(submission.status).isEqualTo(LOADED)
            assertThat(submission.sourceFile).isEqualTo(gzipFilePath)
            assertThat(submission.posInFile).isEqualTo(0)
            assertThat(submission.files).isEmpty()

            val deserializedSubmission = serializationService.deserializeSubmission(submission.body, SubFormat.JSON)
            assertThat(deserializedSubmission.accNo).isEqualTo(ACC_NO)
            assertThat(deserializedSubmission.section.type).isEqualTo("Study")
            assertThat(deserializedSubmission.section.links.first()).hasLeftValueSatisfying {
                assertThat(it.url).isEqualTo("Types")
                assertThat(it.attributes).hasSize(2)
                assertThat(it.attributes.first().name).isEqualTo("AM905938")
                assertThat(it.attributes.first().value).isEqualTo("ENA")
                assertThat(it.attributes.second().name).isEqualTo("P12004")
                assertThat(it.attributes.second().value).isEqualTo("uniprot")
            }
            assertThat(deserializedSubmission.attributes).isEqualTo(listOf(SUB_ATTRIBUTE))
        }
    }
}
