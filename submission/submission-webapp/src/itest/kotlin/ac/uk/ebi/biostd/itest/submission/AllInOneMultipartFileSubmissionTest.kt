package ac.uk.ebi.biostd.itest.submission

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import arrow.core.Either
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.File
import ebi.ac.uk.test.createFile
import ebi.ac.uk.test.createNewFile
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
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Paths
import java.io.File as SystemFile

@ExtendWith(TemporaryFolderExtension::class)
internal class AllInOneMultipartFileSubmissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Transactional
    @DirtiesContext
    inner class SingleSubmissionTest(
        @Autowired val submissionRepository: SubmissionRepository,
        @Autowired val serializationService: SerializationService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(SuperUser.asRegisterRequest())
            webClient = securityClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
        }

        @Test
        fun `submit multipart JSON submission file`() {
            testSubmission("SimpleAcc1", SubFormat.JSON, "json")
        }

        @Test
        fun `submit multipart XML submission file`() {
            testSubmission("SimpleAcc2", SubFormat.XML, "xml")
        }

        @Test
        fun `submit multipart TSV submission file`() {
            testSubmission("SimpleAcc3", SubFormat.TSV, "tsv")
        }

        private fun testSubmission(accNo: String, format: SubFormat, extension: String) {
            val dataFile = tempFolder.createNewFile("DataFile.txt")
            val submissionFile = createSubmission(accNo, dataFile, format, extension)

            val response = webClient.submitSingle(submissionFile, listOf(dataFile))

            assertSuccessfulResponse(response)

            val createdSubmission = submissionRepository.getExtendedByAccNo(accNo)
            assertThat(createdSubmission).hasAccNo(accNo)
            assertThat(createdSubmission.section.files).containsExactly(Either.left(File(dataFile.name)))

            val submissionFolderPath = "$basePath/submission/${createdSubmission.relPath}/Files"
            assertThat(Paths.get("$submissionFolderPath/${dataFile.name}")).exists()
        }

        private fun <T> assertSuccessfulResponse(response: ResponseEntity<T>) {
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull
        }

        private fun createSubmission(accNo: String, dataFile: SystemFile, format: SubFormat, ext: String): SystemFile {
            val submission = submission(accNo) { section(type = "Study") { file(dataFile.name) } }
            val submissionFileContent = serializationService.serializeSubmission(submission, format)
            return tempFolder.createFile("$accNo.$ext", submissionFileContent)
        }
    }
}
