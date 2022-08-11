package ac.uk.ebi.biostd.itest.test.submission.query

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URLEncoder.encode

@Import(PersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionListApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)

        for (idx in 11..20) {
            assertThat(webClient.submitSingle(getSimpleSubmission(idx), TSV)).isSuccessful()
        }

        val filesConfig = SubmissionFilesConfig(emptyList())
        for (idx in 21..30) {
            val submission = tempFolder.createFile("submission$idx.tsv", getSimpleSubmission(idx))
            assertThat(webClient.submitSingle(submission, filesConfig)).isSuccessful()
        }
    }

    @Test
    @Disabled("If submission is processed fast enough test wil fail. Needs re design.")
    fun `get submission when processing`() {
        val newVersion = getSimpleSubmission(18)
        webClient.submitAsync(newVersion, TSV)

        val submissionList = webClient.getSubmissions(mapOf("accNo" to "SimpleAcc18"))

        assertThat(submissionList).anySatisfy {
            assertThat(it.accno).isEqualTo("SimpleAcc18")
            assertThat(it.version).isEqualTo(2)
            assertThat(it.method).isEqualTo(PAGE_TAB)
            assertThat(it.title).isEqualTo("Simple Submission 18 - keyword18")
            assertThat(it.status).isEqualTo("REQUESTED")
        }
    }

    @Test
    fun `get submission list`() {
        val submissionList = webClient.getSubmissions()

        assertThat(submissionList).isNotNull
        assertThat(submissionList).hasSize(15)
    }

    @Test
    fun `get submission list by accession`() {
        val submissionList = webClient.getSubmissions(
            mapOf(
                "accNo" to "LIST-API-17"
            )
        )

        assertThat(submissionList).hasOnlyOneElementSatisfying {
            assertThat(it.accno).isEqualTo("LIST-API-17")
            assertThat(it.version).isEqualTo(1)
            assertThat(it.method).isEqualTo(PAGE_TAB)
            assertThat(it.title).isEqualTo("Simple Submission 17 - list-api-keyword-17")
            assertThat(it.status).isEqualTo("PROCESSED")
        }
    }

    @Test
    fun `get direct submission list by accession`() {
        val submissionList = webClient.getSubmissions(
            mapOf(
                "accNo" to "LIST-API-27"
            )
        )

        assertThat(submissionList).hasOnlyOneElementSatisfying {
            assertThat(it.accno).isEqualTo("LIST-API-27")
            assertThat(it.version).isEqualTo(1)
            assertThat(it.method).isEqualTo(SubmissionMethod.FILE)
            assertThat(it.title).isEqualTo("Simple Submission 27 - list-api-keyword-27")
            assertThat(it.status).isEqualTo("PROCESSED")
        }
    }

    @Test
    fun `get submission list by keywords`() {
        val submissionList = webClient.getSubmissions(
            mapOf(
                "keywords" to "list-api-keyword-20"
            )
        )

        assertThat(submissionList).hasOnlyOneElementSatisfying {
            assertThat(it.title).contains("list-api-keyword-20")
        }
    }

    @Test
    fun `get submission list by release date`() {
        val submissionList = webClient.getSubmissions(
            mapOf(
                "rTimeFrom" to "2019-09-24T09:41:44.000Z",
                "rTimeTo" to "2019-09-28T09:41:44.000Z"
            )
        )

        assertThat(submissionList).hasSize(4)
    }

    @Test
    fun `get submission list pagination`() {
        val submissionList = webClient.getSubmissions(
            mapOf(
                "offset" to 15,
                "keywords" to "list-api-keyword"
            )
        )

        assertThat(submissionList).hasSize(5)
    }

    @Test
    fun `get submissions with submission title`() {
        val submission = tsv {
            line("Submission", "SECT-123")
            line("Title", "Submission subTitle")
            line()

            line("Study")
            line("Title", "Submission With Section")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val submissionList = webClient.getSubmissions(
            mapOf(
                "keywords" to "subTitle"
            )
        )

        assertThat(submissionList).hasOnlyOneElementSatisfying {
            assertThat(it.accno).isEqualTo("SECT-123")
            assertThat(it.version).isEqualTo(1)
            assertThat(it.method).isEqualTo(PAGE_TAB)
            assertThat(it.title).isEqualTo("Submission subTitle")
            assertThat(it.status).isEqualTo("PROCESSED")
        }
    }

    @Test
    fun `get submissions with section title`() {
        val submission = tsv {
            line("Submission", "SECT-124")
            line()

            line("Study")
            line("Title", "Section secTitle")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val submissionTitleList = webClient.getSubmissions(mapOf("keywords" to "secTitle"))
        assertThat(submissionTitleList).hasOnlyOneElementSatisfying {
            assertThat(it.accno).isEqualTo("SECT-124")
            assertThat(it.version).isEqualTo(1)
            assertThat(it.method).isEqualTo(PAGE_TAB)
            assertThat(it.title).isEqualTo("Section secTitle")
            assertThat(it.status).isEqualTo("PROCESSED")
        }
    }

    @Test
    fun `submission with spaces`() {
        val submission = tsv {
            line("Submission", "SECT-125")
            line("Title", "the Submission title")
            line()

            line("Study")
            line("Title", "the Submission title")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val submissionList = webClient.getSubmissions(
            mapOf(
                "keywords" to encode("n title", "UTF-8")
            )
        )

        assertThat(submissionList).hasOnlyOneElementSatisfying {
            assertThat(it.accno).isEqualTo("SECT-125")
            assertThat(it.version).isEqualTo(1)
            assertThat(it.method).isEqualTo(PAGE_TAB)
            assertThat(it.title).isEqualTo("the Submission title")
            assertThat(it.status).isEqualTo("PROCESSED")
        }
    }

    @Test
    fun `latest updated submission should appear first`() {
        webClient.submitSingle(getSimpleSubmission(19), TSV)

        val submissionList = webClient.getSubmissions(
            mapOf("keywords" to "list-api-keyword")
        )

        assertThat(submissionList.first().accno).isEqualTo("LIST-API-19")
    }

    private fun getSimpleSubmission(idx: Int) = tsv {
        line("Submission", "LIST-API-$idx")
        line("Title", "Simple Submission $idx - list-api-keyword-$idx")
        line("ReleaseDate", "2019-09-$idx")
        line()
    }.toString()
}
