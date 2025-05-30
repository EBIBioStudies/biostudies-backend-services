package ac.uk.ebi.biostd.itest.test.submission.query

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.doc.migrations.ensureSubmissionIndexes
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import kotlinx.coroutines.runBlocking
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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URLEncoder.encode

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SubmissionListApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val mongoTemplate: ReactiveMongoTemplate,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            mongoTemplate.ensureSubmissionIndexes()
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)

            for (idx in 11..20) {
                assertThat(webClient.submit(getSimpleSubmission(idx), TSV)).isSuccessful()
            }

            val params = SubmitParameters(storageMode = storageMode)
            for (idx in 21..30) {
                val submission = tempFolder.createFile("submission$idx.tsv", getSimpleSubmission(idx))
                assertThat(webClient.submitMultipart(submission, params)).isSuccessful()
            }
        }

    @Test
    fun `13-1 get submission list`() =
        runTest {
            val submissionList = webClient.getSubmissions()

            assertThat(submissionList).isNotNull
            assertThat(submissionList).hasSize(15)
        }

    @Test
    fun `13-2 get submission list by accession`() =
        runTest {
            val submissionList =
                webClient.getSubmissions(
                    mapOf(
                        "accNo" to "LIST-API-17",
                    ),
                )

            assertThat(submissionList).satisfiesOnlyOnce {
                assertThat(it.accno).isEqualTo("LIST-API-17")
                assertThat(it.title).isEqualTo("Simple Submission 17 - list-api-keyword-17")
                assertThat(it.status).isEqualTo("PROCESSED")
            }
        }

    @Test
    fun `13-3 get direct submission list by accession`() =
        runTest {
            val submissionList =
                webClient.getSubmissions(
                    mapOf(
                        "accNo" to "LIST-API-27",
                    ),
                )

            assertThat(submissionList).satisfiesOnlyOnce {
                assertThat(it.accno).isEqualTo("LIST-API-27")
                assertThat(it.title).isEqualTo("Simple Submission 27 - list-api-keyword-27")
                assertThat(it.status).isEqualTo("PROCESSED")
            }
        }

    @Test
    fun `13-4 get submission list by keywords`() =
        runTest {
            val submissionList =
                webClient.getSubmissions(
                    mapOf(
                        "keywords" to "list-api-keyword-20",
                    ),
                )

            assertThat(submissionList).satisfiesOnlyOnce {
                assertThat(it.title).contains("list-api-keyword-20")
            }
        }

    @Test
    fun `13-5 get submission list by release date`() =
        runTest {
            val submissionList =
                webClient.getSubmissions(
                    mapOf(
                        "rTimeFrom" to "2119-09-24T09:41:44.000Z",
                        "rTimeTo" to "2119-09-28T09:41:44.000Z",
                    ),
                )

            assertThat(submissionList).hasSize(4)
        }

    @Test
    fun `13-6 get submission list pagination`() =
        runTest {
            val submissionList =
                webClient.getSubmissions(
                    mapOf(
                        "offset" to 15,
                        "keywords" to "list-api-keyword",
                    ),
                )

            assertThat(submissionList).hasSize(5)
        }

    @Test
    fun `13-7 get submissions with submission title`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "SECT-123")
                    line("Title", "Submission subTitle")
                    line()

                    line("Study")
                    line("Title", "Submission With Section")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val submissionList =
                webClient.getSubmissions(
                    mapOf(
                        "keywords" to "subTitle",
                    ),
                )

            assertThat(submissionList).satisfiesOnlyOnce {
                assertThat(it.accno).isEqualTo("SECT-123")
                assertThat(it.title).isEqualTo("Submission With Section")
                assertThat(it.status).isEqualTo("PROCESSED")
            }
        }

    @Test
    fun `13-8 get submissions with section title`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "SECT-124")
                    line()

                    line("Study")
                    line("Title", "Section secTitle")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val submissionTitleList = webClient.getSubmissions(mapOf("keywords" to "secTitle"))
            assertThat(submissionTitleList).satisfiesOnlyOnce {
                assertThat(it.accno).isEqualTo("SECT-124")
                assertThat(it.title).isEqualTo("Section secTitle")
                assertThat(it.status).isEqualTo("PROCESSED")
            }
        }

    @Test
    fun `13-9 search submission with spaces`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "SECT-125")
                    line("Title", "the Submission spaces title")
                    line()

                    line("Study")
                    line("Title", "the Submission spaces title")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val submissionList =
                webClient.getSubmissions(
                    mapOf(
                        "keywords" to encode("spaces title", "UTF-8"),
                    ),
                )

            assertThat(submissionList).satisfiesOnlyOnce {
                assertThat(it.accno).isEqualTo("SECT-125")
                assertThat(it.title).isEqualTo("the Submission spaces title")
                assertThat(it.status).isEqualTo("PROCESSED")
            }
        }

    @Test
    fun `13-10 latest updated submission should appear first`() =
        runTest {
            webClient.submit(getSimpleSubmission(19), TSV)

            val submissionList =
                webClient.getSubmissions(
                    mapOf("keywords" to "list-api-keyword"),
                )

            assertThat(submissionList.first().accno).isEqualTo("LIST-API-19")
        }

    private fun getSimpleSubmission(idx: Int) =
        tsv {
            line("Submission", "LIST-API-$idx")
            line("Title", "Simple Submission $idx - list-api-keyword-$idx")
            line("ReleaseDate", "2119-09-$idx")
            line()
        }.toString()
}
