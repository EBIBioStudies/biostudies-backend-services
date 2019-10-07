package ac.uk.ebi.biostd.itest.submission

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.extensions.releaseTime
import ebi.ac.uk.model.extensions.title
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionListApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, TestConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(SuperUser.asRegisterRequest())
            securityClient.registerUser(RegularUser.asRegisterRequest())

            webClient = securityClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
            for (i in 1..20) {
                val submission = submission("SimpleAcc$i") {
                    title = "Simple Submission $i - keyword$i"
                    releaseTime = OffsetDateTime.now().plusDays(i.toLong())
                }
                webClient.submitSingle(submission, SubmissionFormat.JSON)
            }
        }

        @Test
        fun `get submission list`() {
            val submissionList = webClient.getSubmissions()
            assertThat(submissionList).isNotNull
            assertThat(submissionList).hasSize(15)
            assertThat(submissionList).isSortedAccordingTo { a, b -> b.rtime.compareTo(a.rtime) }
        }

        @Test
        fun `get submission list by accession`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "accNo" to "SimpleAcc10"
            ))
            assertThat(submissionList).hasOnlyOneElementSatisfying {
                assertThat(it.accno).isEqualTo("SimpleAcc10")
            }
        }

        @Test
        fun `get submission list by keywords`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "keywords" to "keyword20"
            ))
            assertThat(submissionList).hasOnlyOneElementSatisfying {
                assertThat(it.title).contains("keyword20")
            }
        }

        @Test
        fun `get submission list pagination`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "offset" to 15
            ))
            assertThat(submissionList).hasSize(5)
        }
    }
}
