package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.security.integration.model.SignUpRequest
import ebi.ac.uk.security.service.SecurityService
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
@TestInstance(PER_CLASS)
internal class MultipartSubmissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {

    @Nested
    @TestInstance(PER_CLASS)
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class SingleSubmissionTest {

        @LocalServerPort
        private var serverPort: Int = 0

        @Autowired
        private lateinit var securityService: SecurityService

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityService.registerUser(SignUpRequest("test@biostudies.com", "jhon_doe", "12345"))
            webClient = BioWebClient.create("http://localhost:$serverPort", securityService.login("jhon_doe", "12345"))
        }

        @Test
        fun `submit multipart JSON submission`() {
            val file = tempFolder.createFile("DataFile1.txt")
            val submission = submission("SimpleAcc1") {
                section(type = "Study") {
                    file(file.name)
                }
            }

            val response = webClient.submitSingle(submission, SubmissionFormat.JSON, listOf(file))
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }
    }
}
