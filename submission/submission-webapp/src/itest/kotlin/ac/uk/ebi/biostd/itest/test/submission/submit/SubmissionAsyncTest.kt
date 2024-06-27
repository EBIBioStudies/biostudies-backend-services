package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import ebi.ac.uk.model.RequestStatus.INDEXED
import ebi.ac.uk.model.RequestStatus.INDEXED_CLEANED
import ebi.ac.uk.model.RequestStatus.LOADED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.RequestStatus.REQUESTED
import ebi.ac.uk.model.extensions.title
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.ONE_SECOND
import org.awaitility.Durations.TWO_SECONDS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration.ofMillis

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionAsyncTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val requestRepository: SubmissionRequestPersistenceService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @Autowired val extSubmissionSubmitter: ExtSubmissionSubmitter,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `19-1 simple submit async`(): Unit =
        runTest {
            val submission =
                tsv {
                    line("Submission", "SimpleAsync1")
                    line("Title", "Async Submission")
                    line()

                    line("RootSection")
                    line()
                }.toString()

            val (accNo, version) = webClient.submitAsync(submission, TSV)
            waitUntil(timeout = ONE_SECOND) { submissionRepository.existByAccNoAndVersion(accNo, version) }

            val saved = toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo("SimpleAsync1"))
            assertThat(saved).isEqualTo(
                submission("SimpleAsync1") {
                    title = "Async Submission"
                    section("RootSection") {}
                },
            )
        }

    @Test
    fun `19-2 check submission stages`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "SimpleAsync2")
                    line("Title", "Submission Stages")
                    line()

                    line("RootSection")
                    line()
                }.toString()

            webClient.submitSingle(submission, TSV)

            val extSubmission = submissionRepository.getExtByAccNo("SimpleAsync2")
            val extSubmitRequest = ExtSubmitRequest(extSubmission, SuperUser.email)

            extSubmissionSubmitter.createRequest(extSubmitRequest)
            val statusAfterCreation = requestRepository.getRequestStatus("SimpleAsync2", 2)
            assertThat(statusAfterCreation).isEqualTo(REQUESTED)

            suspend fun assertStageExecution(status: RequestStatus) {
                waitUntil(checkInterval = ofMillis(10), timeout = TWO_SECONDS) {
                    requestRepository.getRequestStatus("SimpleAsync2", 2) == status
                }
            }

            extSubmissionSubmitter.indexRequest("SimpleAsync2", 2)
            assertStageExecution(INDEXED)
            assertStageExecution(LOADED)
            assertStageExecution(INDEXED_CLEANED)
            assertStageExecution(CLEANED)
            assertStageExecution(FILES_COPIED)
            assertStageExecution(PERSISTED)
            assertStageExecution(PROCESSED)

            assertThat(submissionRepository.existByAccNoAndVersion("SimpleAsync2", 1)).isFalse()
            assertThat(submissionRepository.existByAccNoAndVersion("SimpleAsync2", -1)).isTrue()
            assertThat(submissionRepository.existByAccNoAndVersion("SimpleAsync2", 2)).isTrue()
        }
}
