package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.invalidLinkUrl
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.ftpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import kotlin.test.assertFailsWith

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SubmissionApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val sequenceRepository: SequenceDataRepository,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)

        sequenceRepository.save(DbSequence("S-BSST"))
    }

    @Test
    fun `16-1 submit with submission object`() {
        val submission = submission("SimpleAcc1") {
            title = "Simple Submission"
        }

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(getSimpleSubmission("SimpleAcc1")).isEqualTo(
            submission("SimpleAcc1") {
                title = "Simple Submission"
            }
        )
    }

    @Test
    fun `16-2 empty accNo`() {
        val submission = tsv {
            line("Submission")
            line("Title", "Empty AccNo")
        }.toString()

        val response = webClient.submitSingle(submission, TSV)

        assertThat(response).isSuccessful()
        assertThat(getSimpleSubmission(response.body.accNo)).isEqualTo(
            submission(response.body.accNo) {
                title = "Empty AccNo"
            }
        )
    }

    @Test
    fun `16-3 submission with root path`() {
        val submission = tsv {
            line("Submission", "S-12364")
            line("Title", "Sample Submission")
            line("RootPath", "RootPathFolder")
            line()

            line("Study")
            line()

            line("File", "DataFile5.txt")
            line()
        }.toString()

        tempFolder.createDirectory("RootPathFolder")
        webClient.uploadFiles(
            listOf(tempFolder.createFile("RootPathFolder/DataFile5.txt", "An example content")),
            "RootPathFolder"
        )

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(getSimpleSubmission("S-12364")).isEqualTo(
            submission("S-12364") {
                title = "Sample Submission"
                rootPath = "RootPathFolder"
                section("Study") { file("DataFile5.txt") }
            }
        )
    }

    @Test
    fun `16-4 submission with generic root section`() {
        val submission = tsv {
            line("Submission", "E-MTAB123")
            line("Title", "Generic Submission")
            line()

            line("Experiment")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(getSimpleSubmission("E-MTAB123")).isEqualTo(
            submission("E-MTAB123") {
                title = "Generic Submission"
                section("Experiment") { }
            }
        )
    }

    @Test
    fun `16-5 submit with invalid link Url`() {
        val exception = assertThrows(WebClientException::class.java) {
            webClient.submitSingle(invalidLinkUrl().toString(), TSV)
        }

        assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `16-6 submission with validation error`() {
        val submission = submission("S-400") {
            title = "Submission with invalid file"
            section("Study") { file("invalidfile.txt") }
        }

        val exception = assertFailsWith<WebClientException> {
            webClient.submitSingle(submission, SubmissionFormat.XML)
        }
        assertThat(exception.message!!.contains("Submission contains invalid files invalid file.txt"))
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `16-7 submission for checking ftp files`() {
        val submission = tsv {
            line("Submission", "S-500")
            line("Title", "Submission")
            line("ReleaseDate", "2020-01-25")
            line()

            line("Study")
            line()

            line("File", "folder1")
            line()
        }.toString()

        webClient.createFolder("folder1")
        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val submitted = submissionRepository.getExtByAccNo("S-500")

        assertThat(File("$ftpPath/${submitted.relPath}/Files").listFiles()?.size).isEqualTo(1)
        assertThat(File("$ftpPath/${submitted.relPath}/Files/folder1")).exists()
        assertThat(File("$ftpPath/${submitted.relPath}/Files/folder1")).isEmptyDirectory()
    }

    @Nested
    inner class SubmitBaseSubmissionRelPath {
        @Nested
        @SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["app.subBasePath=subRelPathSlash/"])
        inner class RelPathWithSlash(@LocalServerPort val serverPort: Int) {
            private lateinit var webClient: BioWebClient

            @BeforeAll
            fun init() {
                webClient = getWebClient(serverPort, SuperUser)
            }

            @Test
            fun `16-10 submission with file when baseSubmissionRelPath has slash`() {
                val submission = tsv {
                    line("Submission", "S-12365")
                    line("Title", "Sample Submission")
                    line()

                    line("Study")
                    line()

                    line("File", "file12365.txt")
                    line()
                }.toString()
                webClient.uploadFiles(listOf(tempFolder.createFile("file12365.txt", "An example content")))

                assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

                val extSub = submissionRepository.getExtByAccNo("S-12365")
                assertThat(extSub.relPath).isEqualTo("subRelPathSlash/S-/365/S-12365")
            }
        }

        @Nested
        @SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["app.subBasePath=subRelPath"])
        inner class RelPathWithNoSlash(@LocalServerPort val serverPort: Int) {
            private lateinit var webClient: BioWebClient

            @BeforeAll
            fun init() {
                webClient = getWebClient(serverPort, SuperUser)
            }

            @Test
            fun `16-11 submission with file when submissionRelPath was no slash`() {
                val submission = tsv {
                    line("Submission", "S-12366")
                    line("Title", "Sample Submission")
                    line()

                    line("Study")
                    line()

                    line("File", "file12366.txt")
                    line()
                }.toString()
                webClient.uploadFiles(listOf(tempFolder.createFile("file12366.txt", "An example content")))

                assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

                val extSub = submissionRepository.getExtByAccNo("S-12366")
                assertThat(extSub.relPath).isEqualTo("subRelPath/S-/366/S-12366")
            }
        }
    }

    private fun getSimpleSubmission(accNo: String) =
        toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo(accNo))
}
