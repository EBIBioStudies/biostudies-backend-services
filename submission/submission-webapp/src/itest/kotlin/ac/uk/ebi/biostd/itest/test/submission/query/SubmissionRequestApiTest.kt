package ac.uk.ebi.biostd.itest.test.submission.query

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.asserts.assertThatThrows
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.RequestStatus.POST_PROCESSED
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime
import ebi.ac.uk.asserts.assertThat as assertSubmit

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionRequestApiTest(
    @LocalServerPort private val serverPort: Int,
    @Autowired private val securityTestService: SecurityTestService,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `29-1 Get submission request status`() =
        runTest {
            val sub =
                tsv {
                    line("Submission", "S-RQT1")
                    line("Title", "Get Submission Request")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                }.toString()

            assertSubmit(webClient.submit(sub, TSV)).isSuccessful()
            assertThat(webClient.getSubmissionRequestStatus("S-RQT1", 1)).isEqualTo(POST_PROCESSED)
        }

    @Test
    fun `29-2 Archive submission request`() =
        runTest {
            val sub =
                tsv {
                    line("Submission", "S-RQT2")
                    line("Title", "Archive Submission Request")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_29-2.txt")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file_29-2.txt", "29-2 file content"))
            assertSubmit(webClient.submit(sub, TSV)).isSuccessful()
            assertThat(webClient.getSubmissionRequestStatus("S-RQT2", 1)).isEqualTo(POST_PROCESSED)

            webClient.archiveSubmissionRequest("S-RQT2", 1)
            assertThatThrows<WebClientException> { webClient.getSubmissionRequestStatus("S-RQT2", 1) }
                .hasMessageContaining("The submission request 'S-RQT2', version: 1 was not found")
        }
}
