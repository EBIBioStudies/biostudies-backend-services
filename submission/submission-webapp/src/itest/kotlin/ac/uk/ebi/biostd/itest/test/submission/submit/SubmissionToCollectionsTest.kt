package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.TestCollectionValidator
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitAttribute
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.title
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
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime
import kotlin.test.assertFailsWith

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionToCollectionsTest(
    @Autowired private val securityTestService: SecurityTestService,
    @Autowired private val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired private val testCollectionValidator: TestCollectionValidator,
    @Autowired private val toSubmissionMapper: ToSubmissionMapper,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
            setUpCollections()
        }

    @Test
    fun `8-1 accNo generation from collection template`() =
        runTest {
            val submission =
                tsv {
                    line("Submission")
                    line("AttachTo", "Test-Project")
                    line("Title", "AccNo Generation Test")
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            val expected =
                submission("S-TEST-EXT1") {
                    title = "AccNo Generation Test"
                    attachTo = "Test-Project"
                }
            assertThat(getSimpleSubmission("S-TEST-EXT1")).isEqualTo(expected)
        }

    @Test
    fun `8-2 direct submission overriding collection`() =
        runTest {
            val submissionFile =
                tempFolder.createFile(
                    "submission.tsv",
                    tsv {
                        line("Submission", "S-TEST1")
                        line("AttachTo", "Test-Project")
                        line("Title", "Overridden Project")
                    }.toString(),
                )

            val params =
                SubmitParameters(
                    storageMode = storageMode,
                    attributes = listOf(SubmitAttribute("AttachTo", "Public-Project")),
                )
            assertThat(webClient.submitMultipart(submissionFile, params)).isSuccessful()

            assertThat(getSimpleSubmission("S-TEST1")).isEqualTo(
                submission("S-TEST1") {
                    title = "Overridden Project"
                    attachTo = "Public-Project"
                },
            )
        }

    @Test
    fun `8-3 no release date to private collection`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-PRP0")
                    line("AttachTo", "Private-Project")
                    line("Title", "No Release Date To Private Project")
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(getSimpleSubmission("S-PRP0")).isEqualTo(
                submission("S-PRP0") {
                    title = "No Release Date To Private Project"
                    attachTo = "Private-Project"
                },
            )
        }

    @Test
    fun `8-4 public submission to private collection`() =
        runTest {
            val today = OffsetDateTime.now().toStringDate()
            val submission =
                tsv {
                    line("Submission", "S-PRP1")
                    line("AttachTo", "Private-Project")
                    line("ReleaseDate", today)
                    line("Title", "Public Submission To Private Project")
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(getSimpleSubmission("S-PRP1")).isEqualTo(
                submission("S-PRP1") {
                    title = "Public Submission To Private Project"
                    releaseDate = today
                    attachTo = "Private-Project"
                },
            )
        }

    @Test
    fun `8-5 private submission to public collection`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-PUP0")
                    line("AttachTo", "Public-Project")
                    line("ReleaseDate", "2050-12-24")
                    line("Title", "Private submission into public project")
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(getSimpleSubmission("S-PUP0")).isEqualTo(
                submission("S-PUP0") {
                    title = "Private submission into public project"
                    releaseDate = "2050-12-24"
                    attachTo = "Public-Project"
                },
            )
        }

    @Test
    fun `8-6 no release date to public collection`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-PUP1")
                    line("AttachTo", "Public-Project")
                    line("Title", "No Release Date To Public Project")
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(getSimpleSubmission("S-PUP1")).isEqualTo(
                submission("S-PUP1") {
                    title = "No Release Date To Public Project"
                    attachTo = "Public-Project"
                },
            )
        }

    @Test
    fun `8-7 submit to collection with validator`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-VLD0")
                    line("AttachTo", "ValidatedCollection")
                    line("Title", "A Validated Submission")
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(testCollectionValidator.validated).isTrue
            assertThat(getSimpleSubmission("S-VLD0")).isEqualTo(
                submission("S-VLD0") {
                    title = "A Validated Submission"
                    attachTo = "ValidatedCollection"
                },
            )
        }

    @Test
    fun `8-8 submit to collection with failling validator`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-FLC0")
                    line("AttachTo", "FailCollection")
                    line("Title", "A Fail Submission")
                }.toString()

            val exception = assertThrows<WebClientException> { webClient.submit(submission, TSV) }
            assertThat(exception.message!!.contains("Testing failure"))
        }

    @Test
    fun `8-9 admin user provides accNo`() =
        runTest {
            securityTestService.ensureUserRegistration(RegularUser)
            val regularClient = getWebClient(serverPort, RegularUser)
            val submission =
                tsv {
                    line("Submission", "S-PROVIDED1")
                    line("AttachTo", "Test-Project")
                    line()
                    line("Study")
                    line()
                }.toString()

            webClient.grantPermission(RegularUser.email, "Test-Project", ADMIN.name)
            assertThat(regularClient.submit(submission, TSV)).isSuccessful()
        }

    @Test
    fun `8-10 regular user provides accNo`() =
        runTest {
            securityTestService.ensureUserRegistration(RegularUser)
            val regularClient = getWebClient(serverPort, RegularUser)
            val submission =
                tsv {
                    line("Submission", "S-PROVIDED2")
                    line("AttachTo", "Private-Project")
                    line()
                    line("Study")
                    line()
                }.toString()

            val errorMessage = "The user regular@ebi.ac.uk is not allowed to submit to Private-Project collection"
            val exception = assertFailsWith<WebClientException> { regularClient.submit(submission, TSV) }
            assertThat(exception.message!!.contains(errorMessage))
        }

    private suspend fun setUpCollections() {
        val testProject =
            tsv {
                line("Submission", "Test-Project")
                line("AccNoTemplate", "!{S-TEST-EXT}")
                line()

                line("Project")
            }.toString()

        val privateProject =
            tsv {
                line("Submission", "Private-Project")
                line("AccNoTemplate", "!{S-PRP-EXT}")
                line()

                line("Project")
            }.toString()

        val publicProject =
            tsv {
                line("Submission", "Public-Project")
                line("AccNoTemplate", "!{S-PUP-EXT}")
                line("ReleaseDate", OffsetDateTime.now().toStringDate())
                line()

                line("Project")
            }.toString()

        val validatedCollection =
            tsv {
                line("Submission", "ValidatedCollection")
                line("AccNoTemplate", "!{S-VLD-EXT}")
                line("CollectionValidator", "TestCollectionValidator")
                line()

                line("Project")
            }.toString()

        val failCollection =
            tsv {
                line("Submission", "FailCollection")
                line("AccNoTemplate", "!{S-FLC-EXT}")
                line("CollectionValidator", "FailCollectionValidator")
                line()

                line("Project")
            }.toString()

        assertThat(webClient.submit(testProject, TSV)).isSuccessful()
        assertThat(webClient.submit(publicProject, TSV)).isSuccessful()
        assertThat(webClient.submit(privateProject, TSV)).isSuccessful()
        assertThat(webClient.submit(failCollection, TSV)).isSuccessful()
        assertThat(webClient.submit(validatedCollection, TSV)).isSuccessful()
    }

    private suspend fun getSimpleSubmission(accNo: String) =
        toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo(accNo))
}
