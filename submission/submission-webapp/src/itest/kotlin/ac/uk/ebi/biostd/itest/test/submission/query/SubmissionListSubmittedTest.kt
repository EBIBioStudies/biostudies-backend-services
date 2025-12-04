package ac.uk.ebi.biostd.itest.test.submission.query

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.RequestStatus.SUBMITTED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import ebi.ac.uk.util.collections.second
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["app.asyncMode=true"])
class SubmissionListSubmittedTest(
    @param:Autowired val requestRepository: SubmissionRequestPersistenceService,
    @param:Autowired val securityTestService: SecurityTestService,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SubmissionUser)
            webClient = getWebClient(serverPort, SubmissionUser)
        }

    @Test
    fun `13-11 list submission request in SUBMITTED stage`() =
        runTest {
            val delayCollection =
                tsv {
                    line("Submission", "DelayCollection")
                    line("AccNoTemplate", "!{S-DELAY}")
                    line("CollectionValidator", "DelayCollectionValidator")
                    line()

                    line("Project")
                }.toString()

            assertThat(webClient.submit(delayCollection, TSV)).isSuccessful()

            val submission =
                tsv {
                    line("Submission", "S-DELAY1")
                    line("AttachTo", "DelayCollection")
                    line("ReleaseDate", "2150-09-21")
                    line("Title", "A Delayed Processing Submission")
                    line()

                    line("Study")
                }.toString()

            launch(UnconfinedTestDispatcher(testScheduler)) {
                webClient.submitAsync(submission, TSV)
            }

            launch(UnconfinedTestDispatcher(testScheduler)) {
                waitUntil(
                    timeout = Duration.ofSeconds(30),
                ) { requestRepository.getRequest("S-DELAY1", 0).status == SUBMITTED }

                val submissionList = webClient.getSubmissions()
                assertThat(submissionList).hasSize(2)
                assertThat(submissionList.first().accno).isEqualTo("S-DELAY1")
                assertThat(submissionList.first().title).isEqualTo("A Delayed Processing Submission")
                assertThat(submissionList.first().rtime).isEqualTo(OffsetDateTime.of(2150, 9, 21, 0, 0, 0, 0, UTC))
                assertThat(submissionList.first().status).isEqualTo(PROCESSING.name)
                assertThat(submissionList.second().accno).isEqualTo("DelayCollection")
                assertThat(submissionList.second().status).isEqualTo(PROCESSED.name)
            }
        }

    /**
     * Represents a bio studies super user.
     */
    object SubmissionUser : TestUser {
        override val username = "Super User"
        override val email = "biostudies-mgmt-list@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
        override val storageMode = StorageMode.NFS
    }
}
