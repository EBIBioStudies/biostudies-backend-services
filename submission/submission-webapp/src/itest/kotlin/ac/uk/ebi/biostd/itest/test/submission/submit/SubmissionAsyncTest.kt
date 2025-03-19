package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.action
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.RequestStatus.CHECK_RELEASED
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import ebi.ac.uk.model.RequestStatus.INDEXED
import ebi.ac.uk.model.RequestStatus.INDEXED_CLEANED
import ebi.ac.uk.model.RequestStatus.LOADED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.RequestStatus.REQUESTED
import ebi.ac.uk.model.RequestStatus.VALIDATED
import ebi.ac.uk.model.extensions.title
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.FIVE_SECONDS
import org.awaitility.Durations.ONE_SECOND
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
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
    fun `19-2 Check submission stages`(): Unit =
        runBlocking {
            val submission =
                tsv {
                    line("Submission", "SimpleAsync2")
                    line("Title", "Submission Stages")
                    line()

                    line("RootSection")
                    line()
                }.toString()

            webClient.submit(submission, TSV)

            val extSubmission = submissionRepository.getExtByAccNo("SimpleAsync2")
            val extSubmitRequest =
                ExtSubmitRequest(
                    notifyTo = SuperUser.email,
                    submission = extSubmission,
                    singleJobMode = false,
                )

            extSubmissionSubmitter.createRqt(extSubmitRequest)
            val statusAfterCreation = requestRepository.getRequest("SimpleAsync2", 2)
            assertThat(statusAfterCreation.status).isEqualTo(REQUESTED)

            extSubmissionSubmitter.handleRequestAsync("SimpleAsync2", 2)
            waitUntil(timeout = Duration.ofMinutes(1), checkInterval = ofMillis(100)) {
                requestRepository.getRequest("SimpleAsync2", 2).status == PROCESSED
            }
            val requestStatus = requestRepository.getRequest("SimpleAsync2", 2).process!!.statusChanges
            assertThat(requestStatus.map { it.status }).containsExactly(
                REQUESTED.action,
                INDEXED.action,
                LOADED.action,
                INDEXED_CLEANED.action,
                VALIDATED.action,
                CLEANED.action,
                FILES_COPIED.action,
                CHECK_RELEASED.action,
                PERSISTED.action,
            )

            assertThat(submissionRepository.existByAccNoAndVersion("SimpleAsync2", 1)).isFalse()
            assertThat(submissionRepository.existByAccNoAndVersion("SimpleAsync2", -1)).isTrue()
            assertThat(submissionRepository.existByAccNoAndVersion("SimpleAsync2", 2)).isTrue()
        }

    @Test
    fun `19-3 Multiple async submissions with files`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "SMulti-001")
                    line("Title", "Submission")
                    line()

                    line("Study")
                    line()

                    line("File", "file.txt")
                }.toString()

            val submission2 =
                tsv {
                    line("Submission", "SMulti-002")
                    line("Title", "Submission")
                    line()

                    line("Study")
                    line()

                    line("Files")
                    line("file2.txt")
                    line("file3.txt")
                }.toString()

            val file = tempFolder.createFile("file.txt", "content")
            val file2 = tempFolder.createFile("file2.txt", "content")
            val file3 = tempFolder.createFile("file3.txt", "content")

            val result =
                webClient.submitMultipartAsync(
                    format = "tsv",
                    submissions = mapOf("SMulti-001" to submission, "SMulti-002" to submission2),
                    files = mapOf("SMulti-001" to listOf(file), "SMulti-002" to listOf(file2, file3)),
                    parameters = SubmitParameters(storageMode = storageMode),
                )

            assertThat(result).hasSize(2)
            val sub1 = result.first()
            assertThat(sub1.accNo).isEqualTo("SMulti-001")
            assertThat(sub1.version).isEqualTo(1)

            val sub2 = result[1]
            assertThat(sub2.accNo).isEqualTo("SMulti-002")
            assertThat(sub2.version).isEqualTo(1)

            waitUntil(timeout = FIVE_SECONDS) { submissionRepository.existByAccNoAndVersion(sub1.accNo, sub1.version) }
            waitUntil(timeout = FIVE_SECONDS) { submissionRepository.existByAccNoAndVersion(sub2.accNo, sub2.version) }

            val storedSub1 = submissionRepository.getExtByAccNoAndVersion(sub1.accNo, sub1.version)
            assertThat(storedSub1.section.files).hasSize(1)
            val storedF1 = storedSub1.section.files.first()
            assertThat(storedF1)
                .hasLeftValueSatisfying { it.fileName == file.name }

            val storedSub2 = submissionRepository.getExtByAccNoAndVersion(sub2.accNo, sub2.version)
            val storedF2 = storedSub2.section.files.first()
            assertThat(storedF2).hasRightValueSatisfying {
                val files = it.files.map { it.fileName }
                assertThat(files).containsExactly(file2.name, file3.name)
            }
        }

    @Test
    fun `19-4 Multiple async submissions when one invalid`() =
        runTest {
            val submission1 =
                tsv {
                    line("Submission", "SMulti-004")
                    line("Title", "Submission")
                    line()
                }.toString()
            val submission2 =
                tsv {
                    line("Submission", "SMulti-005")
                    line("Title", "Submission")
                    line()

                    line("Study")
                    line()

                    line("File", "sMulti-004-file.txt")
                }.toString()

            val exception =
                assertThrows<WebClientException> {
                    webClient.submitMultipartAsync(
                        format = "tsv",
                        submissions = mapOf("SMulti-004" to submission1, "SMulti-005" to submission2),
                        files = emptyMap(),
                        parameters = SubmitParameters(storageMode = storageMode),
                    )
                }
            assertThat(exception.message).contains("The following files could not be found:\\n  - sMulti-004-file.txt")
        }
}
