package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import arrow.core.Either
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.File
import ebi.ac.uk.security.service.SecurityService
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
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class)
internal class MultipartSubmissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {

    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class SingleSubmissionTest {

        @LocalServerPort
        private var serverPort: Int = 0

        @Autowired
        private lateinit var securityService: SecurityService

        @Autowired
        private lateinit var submissionRepository: SubmissionRepository

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityService.registerUser(RegisterRequest("test@biostudies.com", "jhon_doe", "12345"))
            webClient = SecurityWebClient.create("http://localhost:$serverPort").auhtenticate("jhon_doe", "12345")
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
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull

            val createSubmission = submissionRepository.findExtendedByAccNo(accNo)
            assertThat(createSubmission).hasAccNo(accNo)
            assertThat(createSubmission.section.files).containsExactly(Either.left(File("DataFile1.txt")))

            val submissionFolderPath = "$basePath/${createSubmission.relPath}/Files"
            assertThat(Paths.get("$submissionFolderPath/$fileName")).exists()
        }
    }
}
