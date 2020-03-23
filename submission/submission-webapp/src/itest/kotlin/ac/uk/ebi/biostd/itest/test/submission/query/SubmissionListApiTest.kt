package ac.uk.ebi.biostd.itest.test.submission.query

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.test.createFile
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

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionListApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)

            for (idx in 11..20) {
                assertThat(webClient.submitSingle(getSimpleSubmission(idx), SubmissionFormat.TSV)).isSuccessful()
            }

            for (idx in 21..30) {
                val submission = tempFolder.createFile("submission$idx.tsv", getSimpleSubmission(idx))
                assertThat(webClient.submitSingle(submission, emptyList())).isSuccessful()
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
            val submissionList = webClient.getSubmissions(mapOf(
                "accNo" to "SimpleAcc17"
            ))

            assertThat(submissionList).hasOnlyOneElementSatisfying {
                assertThat(it.accno).isEqualTo("SimpleAcc17")
                assertThat(it.version).isEqualTo(1)
                assertThat(it.method).isEqualTo(SubmissionMethod.PAGE_TAB)
                assertThat(it.title).isEqualTo("Simple Submission 17 - keyword17")
            }
        }

        @Test
        fun `get direct submission list by accession`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "accNo" to "SimpleAcc27"
            ))

            assertThat(submissionList).hasOnlyOneElementSatisfying {
                assertThat(it.accno).isEqualTo("SimpleAcc27")
                assertThat(it.version).isEqualTo(1)
                assertThat(it.method).isEqualTo(SubmissionMethod.FILE)
                assertThat(it.title).isEqualTo("Simple Submission 27 - keyword27")
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
        fun `get submission list by release date`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "rTimeFrom" to "2019-09-24T09:41:44.000Z",
                "rTimeTo" to "2019-09-28T09:41:44.000Z"
            ))

            assertThat(submissionList).hasSize(4)
        }

        @Test
        fun `get submission list pagination`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "offset" to 15
            ))

            assertThat(submissionList).hasSize(5)
        }

        @Test
        fun `submission with section title`() {
            val submission = tsv {
                line("Submission", "SECT-123")
                line()

                line("Study")
                line("Title", "Submission With Section Title")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()

            val submissionList = webClient.getSubmissions(mapOf(
                "accNo" to "SECT-123"
            ))

            assertThat(submissionList).hasOnlyOneElementSatisfying {
                assertThat(it.accno).isEqualTo("SECT-123")
                assertThat(it.version).isEqualTo(1)
                assertThat(it.method).isEqualTo(SubmissionMethod.PAGE_TAB)
                assertThat(it.title).isEqualTo("Submission With Section Title")
            }
        }

        @Test
        fun `submission with both titles`() {
            val submission = tsv {
                line("Submission", "SECT-124")
                line("Title", "Submission Title")
                line()

                line("Study")
                line("Title", "Section Title")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()

            val submissionList = webClient.getSubmissions(mapOf(
                "accNo" to "SECT-124"
            ))

            assertThat(submissionList).hasOnlyOneElementSatisfying {
                assertThat(it.accno).isEqualTo("SECT-124")
                assertThat(it.version).isEqualTo(1)
                assertThat(it.method).isEqualTo(SubmissionMethod.PAGE_TAB)
                assertThat(it.title).isEqualTo("Submission Title")
            }
        }

        private fun getSimpleSubmission(idx: Int) = tsv {
            line("Submission", "SimpleAcc$idx")
            line("Title", "Simple Submission $idx - keyword$idx")
            line("ReleaseDate", "2019-09-$idx")
            line()
        }.toString()
    }
}
