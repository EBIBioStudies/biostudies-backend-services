package uk.ac.ebi.biostd.itest.test.submission.query

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
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
import uk.ac.ebi.biostd.asserts.submitAndWait
import uk.ac.ebi.biostd.dsl.tsv.line
import uk.ac.ebi.biostd.dsl.tsv.tsv
import uk.ac.ebi.biostd.itest.common.SecurityTestService
import uk.ac.ebi.biostd.itest.entities.SuperUser
import uk.ac.ebi.biostd.itest.itest.getWebClient
import uk.ac.ebi.biostd.model.RequestStatus.POST_PROCESSED
import uk.ac.ebi.biostd.util.date.toStringDate
import java.time.OffsetDateTime
import uk.ac.ebi.biostd.asserts.assertThat as assertSubmit

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionRequestApiTest(
    @param:LocalServerPort private val serverPort: Int,
    @param:Autowired private val securityTestService: SecurityTestService,
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

            assertSubmit(webClient.submitAndWait(sub, TSV)).isSuccessful()
            assertThat(webClient.getSubmissionRequestStatus("S-RQT1", 1)).isEqualTo(POST_PROCESSED)
        }
}
