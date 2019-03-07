package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.GenericUser
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import arrow.core.Either
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.File
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class)
internal class MultipartSubmissionApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {

    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest(@Autowired val submissionRepository: SubmissionRepository) {

        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(RegisterRequest(GenericUser.email, GenericUser.username, GenericUser.password))
            webClient = securityClient.getAuthenticatedClient(GenericUser.username, GenericUser.password)
        }

        @Test
        fun `submit multipart JSON submission`() {
            val fileName = "DataFile1.txt"
            val accNo = "SimpleAcc1"

            val file = tempFolder.createFile(fileName)
            val submission = submission(accNo) {
                section(type = "Study") {
                    file(fileName)
                }
            }

            val response = webClient.submitSingle(submission, SubmissionFormat.JSON, listOf(file))
            assertSuccessfulResponse(response)

            val createdSubmission = submissionRepository.getExtendedByAccNo(accNo)
            assertThat(createdSubmission).hasAccNo(accNo)
            assertThat(createdSubmission.section.files).containsExactly(Either.left(File("DataFile1.txt")))

            val submissionFolderPath = "$basePath/submission/${createdSubmission.relPath}/Files"
            assertThat(Paths.get("$submissionFolderPath/$fileName")).exists()
        }

        @Test
        fun `submission with library file`() {
            val submission = tsv {
                line("Submission", "S-TEST1")
                line("Title", "Test Submission")
                line()

                line("Study", "SECT-001")
                line("Title", "Root Section")
                line()

                line("LibraryFile", "LibraryFile.tsv")
            }

            val libraryFile = tempFolder.createFile("LibraryFile.tsv").apply {
                writeBytes(tsv {
                    line("Files", "GEN")
                    line("File1.txt", "ABC")
                }.toString().toByteArray())
            }

            val response = webClient.submitSingle(
                submission.toString(), SubmissionFormat.TSV, listOf(libraryFile, tempFolder.createFile("File1.txt")))
            assertSuccessfulResponse(response)

            val createdSubmission = submissionRepository.getExtendedByAccNo("S-TEST1")
            val submissionFolderPath = "$basePath/submission/${createdSubmission.relPath}"

            assertThat(Paths.get("$submissionFolderPath/Files/File1.txt")).exists()
            assertThat(Paths.get("$submissionFolderPath/S-TEST1.SECT-001.files.tsv")).exists()
            assertThat(Paths.get("$submissionFolderPath/S-TEST1.SECT-001.files.xml")).exists()
            assertThat(Paths.get("$submissionFolderPath/S-TEST1.SECT-001.files.json")).exists()
        }

        private fun <T> assertSuccessfulResponse(response: ResponseEntity<T>) {
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull
        }
    }
}
