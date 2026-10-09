package ac.uk.ebi.pmc.itest

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.pmc.PmcTaskExecutor
import ac.uk.ebi.pmc.config.AppConfig
import ac.uk.ebi.pmc.persistence.docs.InputFileStatus
import ac.uk.ebi.pmc.persistence.docs.SubFileDocument
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFilesDocRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileDocRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionDocRepository
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.json.toJsonQuote
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.linksTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.gZipTo
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.SUBFORMAT
import ebi.ac.uk.model.constants.SUBMISSIONS
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.title
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.time.Duration.ofSeconds

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [AppConfig::class])
@Testcontainers
@DirtiesContext
internal class PmcPipelineIntegrationTest(
    @param:Autowired private val pmcTaskExecutor: PmcTaskExecutor,
    @param:Autowired private val properties: PmcImporterProperties,
    @param:Autowired private val serializationService: SerializationService,
    @param:Autowired private val submissionRepository: SubmissionDocRepository,
    @param:Autowired private val fileRepository: SubFileDocRepository,
    @param:Autowired private val inputFilesRepository: InputFilesDocRepository,
    @param:Autowired private val errorsRepository: ErrorsRepository,
) {
    @TempDir
    lateinit var tempFolder: File

    @Test
    fun `loads processes and submits a PMC export`() =
        runTest {
            val sourceFile = setUpSourceFile()
            val expectedPageTab = serializationService.serializeSubmission(EXPECTED_SUBMISSION, JSON)
            stubExternalApis(expectedPageTab)
            setUpProperties(sourceFile)

            // Load submissions
            properties.mode = PmcMode.LOAD
            pmcTaskExecutor.run()

            val loaded = submissionRepository.findAll().toList().single()
            verifyLoadingStage(sourceFile, expectedPageTab, loaded)

            // Process submissions
            properties.mode = PmcMode.PROCESS
            pmcTaskExecutor.run()

            val processed = submissionRepository.findAll().toList().single()
            val files = fileRepository.findAll().toList()
            verifyProcessingStage(loaded, processed, files)

            // Submit submissions
            properties.mode = PmcMode.SUBMIT
            pmcTaskExecutor.run()

            verifySubmittingStage(loaded, processed, files)
        }

    private suspend fun verifyLoadingStage(
        sourceFile: File,
        expectedPageTab: String,
        loaded: SubmissionDocument,
    ) {
        assertThat(loaded.status).isEqualTo(SubmissionStatus.LOADED)
        assertThat(loaded.accNo).isEqualTo(ACC_NO)
        assertThat(loaded.sourceFile).isEqualTo(sourceFile.absolutePath)
        assertThat(loaded.posInFile).isZero()
        assertThat(loaded.files).isEmpty()
        assertThat(loaded.version).isNull()
        assertThat(serializationService.deserializeSubmission(loaded.body, JSON)).isEqualTo(EXPECTED_SUBMISSION)
        assertThat(loaded.body).isEqualTo(expectedPageTab)
        assertThat(errorsRepository.findAll().toList()).isEmpty()
        assertThat(fileRepository.findAll().toList()).isEmpty()
        assertThat(sourceFile).doesNotExist()
        assertThat(tempFolder.resolve("processed/${sourceFile.name}")).exists()
        val inputFile = inputFilesRepository.findAll().toList().single()
        assertThat(inputFile.name).isEqualTo(sourceFile.absolutePath)
        assertThat(inputFile.status).isEqualTo(InputFileStatus.PROCESSED)
        assertThat(inputFile.error).isNull()
    }

    private suspend fun verifyProcessingStage(
        loaded: SubmissionDocument,
        processed: SubmissionDocument,
        files: List<SubFileDocument>,
    ) {
        assertThat(processed.id).isEqualTo(loaded.id)
        assertThat(processed.status).isEqualTo(SubmissionStatus.PROCESSED)
        assertThat(processed.body).isEqualTo(loaded.body)
        assertThat(processed.version).isNull()
        assertThat(files).hasSize(FILE_CONTENTS.size)
        assertThat(processed.files).containsExactlyInAnyOrderElementsOf(files.map { it.id })
        files.forEach { file ->
            assertThat(file.accNo).isEqualTo(ACC_NO)
            assertThat(File(file.path)).hasContent(FILE_CONTENTS.getValue(file.name))
            wireMockServer.verify(
                1,
                getRequestedFor(urlPathEqualTo(FILES_URL)).withQueryParam("filename", equalTo(file.name)),
            )
        }
        assertThat(errorsRepository.findAll().toList()).isEmpty()
    }

    private suspend fun verifySubmittingStage(
        loaded: SubmissionDocument,
        processed: SubmissionDocument,
        files: List<SubFileDocument>,
    ) {
        val submitted = submissionRepository.findAll().toList().single()
        assertThat(submitted.id).isEqualTo(loaded.id)
        assertThat(submitted.status).isEqualTo(SubmissionStatus.SUBMITTED)
        assertThat(submitted.version).isEqualTo(SUBMITTED_VERSION)
        assertThat(submitted.body).isEqualTo(loaded.body)
        assertThat(submitted.files).containsExactlyInAnyOrderElementsOf(processed.files)
        assertThat(fileRepository.findAll().toList()).containsExactlyInAnyOrderElementsOf(files)
        assertThat(errorsRepository.findAll().toList()).isEmpty()
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(SUBMIT_URL)))
        wireMockServer.verify(3, postRequestedFor(urlEqualTo("/notifications")))
    }

    private fun setUpSourceFile(): File {
        val source = tempFolder.resolve(SOURCE_FILE_NAME)
        source.writeText(SUBMISSION_PAGETAB)
        val sourceGzip = tempFolder.resolve("${source.name}.gz")
        source.gZipTo(sourceGzip)

        return sourceGzip
    }

    private fun setUpProperties(source: File) {
        properties.loadFolder = tempFolder.path
        properties.loadFile = source.name
        properties.sourceFile = source.absolutePath
        properties.temp = tempFolder.resolve("downloads").path
        properties.limit = null
        properties.batchSize = 1
    }

    private fun stubExternalApis(expectedBody: String) {
        wireMockServer.stubFor(
            post(urlEqualTo("/notifications")).willReturn(
                aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON).withBody("ok".toJsonQuote()),
            ),
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/auth/login")).willReturn(
                aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON).withBody(
                    jsonObj {
                        "sessid" to "sessid"
                        "email" to "admin_user@ebi.ac.uk"
                        "username" to "admin_user"
                        "secret" to "secret"
                        "fullname" to "Pipeline test user"
                        "superuser" to true
                        "orcid" to "orcid"
                        "allow" to jsonArray("allow")
                        "deny" to jsonArray("deny")
                        "uploadType" to "nfs"
                        "lastActivity" to "2026-06-07T12:00:00"
                    }.toString(),
                ),
            ),
        )
        FILE_CONTENTS.forEach { (name, content) ->
            wireMockServer.stubFor(
                get(urlPathEqualTo(FILES_URL))
                    .withQueryParam("filename", equalTo(name))
                    .willReturn(aResponse().withBody(content)),
            )
        }
        val submitRequest =
            post(urlEqualTo(SUBMIT_URL))
                .withHeader(CONTENT_TYPE, containing(MULTIPART_FORM_DATA))
                .withMultipartRequestBody(
                    aMultipart()
                        .withName(SUBMISSIONS)
                        .withBody(equalToJson(jsonObj { ACC_NO to expectedBody }.toString())),
                ).withMultipartRequestBody(aMultipart().withName(SUBFORMAT).withBody(equalTo("json")))
        FILE_CONTENTS.forEach { (name, content) ->
            submitRequest.withMultipartRequestBody(
                aMultipart()
                    .withName(ACC_NO)
                    .withHeader(CONTENT_DISPOSITION, containing("filename=\"$name\""))
                    .withBody(equalTo(content)),
            )
        }
        wireMockServer.stubFor(
            submitRequest.willReturn(
                aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON).withBody(
                    jsonArray(
                        jsonObj {
                            "accNo" to ACC_NO
                            "version" to SUBMITTED_VERSION
                        },
                    ).toString(),
                ),
            ),
        )
    }

    companion object {
        private const val ACC_NO = "S-EPMC1234567"
        private const val SOURCE_FILE_NAME = "bs-07-02-2024-6.txt"
        private const val SUBMITTED_VERSION = 3
        private const val DATABASE_NAME = "pmc-pipeline-test"
        private const val FILES_URL = "/files/getFileStream/PMC1234567"
        private const val SUBMIT_URL = "/submissions/async/multiple"
        private val FILE_CONTENTS = mapOf("file1.txt" to "first file content", "file2.txt" to "second file content")
        private val wireMockServer = WireMockServer(WireMockConfiguration().dynamicPort())
        private val SUBMISSION_PAGETAB =
            tsv {
                line("Submission", ACC_NO)
                line("Title", "PMC pipeline submission")
                line()

                line("Study")
                line()

                line("File", "file1.txt")
                line()

                line("Link", "AM905938")
                line("Type", "gen")
                line()

                line("Publication")
                line("Publication date", "2020 May")
                line()

                line("File", "file2.txt")
                line()

                line("Links", "Type")
                line("PRJNA123", "gen")
                line("P12004", "uniprot")
                line()
            }.toString()

        private val EXPECTED_SUBMISSION =
            submission(ACC_NO) {
                title = "PMC pipeline submission"
                releaseDate = "2020-01-01"
                section("Study") {
                    file("file1.txt")
                    link("AM905938") { this["Type"] = "ENA" }
                    section("Publication") {
                        this["Publication date"] = "2020 May"
                        file("file2.txt")
                        linksTable {
                            link("PRJNA123") { this["Type"] = "ENA" }
                            link("P12004") { this["Type"] = "uniprot" }
                        }
                    }
                }
            }

        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(registry: DynamicPropertyRegistry) {
            wireMockServer.start()
            registry.add("app.data.mode") { PmcMode.LOAD.name }
            registry.add("app.data.mongodbUri") { mongoContainer.getReplicaSetUrl(DATABASE_NAME) }
            registry.add("app.data.mongodbDatabase") { DATABASE_NAME }
            registry.add("app.data.pmcBaseUrl") { wireMockServer.baseUrl() }
            registry.add("app.data.bioStudiesUrl") { wireMockServer.baseUrl() }
            registry.add("app.data.bioStudiesUser") { "admin_user@ebi.ac.uk" }
            registry.add("app.data.bioStudiesPassword") { "123456" }
            registry.add("app.data.notificationsUrl") { "${wireMockServer.baseUrl()}/notifications" }
        }

        @JvmStatic
        @AfterAll
        fun stopWireMock() {
            wireMockServer.stop()
        }
    }
}
