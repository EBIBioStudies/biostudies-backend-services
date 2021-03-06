package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.TestCollectionValidator
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.ext.getSimpleByAccNo
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.Collections.singletonMap
import kotlin.test.assertFailsWith

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionToCollectionsTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmitToExtCollectionTest(
        @Autowired private val securityTestService: SecurityTestService,
        @Autowired private val submissionRepository: SubmissionQueryService,
        @Autowired private val testCollectionValidator: TestCollectionValidator
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)

            webClient = getWebClient(serverPort, SuperUser)
            setUpCollections()
        }

        @Test
        fun `accNo generation from collection template`() {
            val submission = tsv {
                line("Submission")
                line("AttachTo", "Test-Project")
                line("Title", "AccNo Generation Test")
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            val expected = submission("S-TEST0") {
                title = "AccNo Generation Test"
                attachTo = "Test-Project"
            }
            assertThat(submissionRepository.getSimpleByAccNo("S-TEST0")).isEqualTo(expected)
        }

        @Test
        fun `direct submission overriding collection`() {
            val submissionFile = tempFolder.createFile(
                "submission.tsv",
                tsv {
                    line("Submission", "S-TEST1")
                    line("AttachTo", "Test-Project")
                    line("Title", "Overridden Project")
                }.toString()
            )

            assertThat(
                webClient.submitSingle(
                    submissionFile, emptyList(), singletonMap("AttachTo", "Public-Project")
                )
            ).isSuccessful()

            assertThat(submissionRepository.getSimpleByAccNo("S-TEST1")).isEqualTo(
                submission("S-TEST1") {
                    title = "Overridden Project"
                    attachTo = "Public-Project"
                }
            )
        }

        @Test
        fun `no release date to private collection`() {
            val submission = tsv {
                line("Submission", "S-PRP0")
                line("AttachTo", "Private-Project")
                line("Title", "No Release Date To Private Project")
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo("S-PRP0")).isEqualTo(
                submission("S-PRP0") {
                    title = "No Release Date To Private Project"
                    attachTo = "Private-Project"
                }
            )
        }

        @Test
        fun `public submission to private collection`() {
            val submission = tsv {
                line("Submission", "S-PRP1")
                line("AttachTo", "Private-Project")
                line("ReleaseDate", "2015-12-24")
                line("Title", "Public Submission To Private Project")
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo("S-PRP1")).isEqualTo(
                submission("S-PRP1") {
                    title = "Public Submission To Private Project"
                    releaseDate = "2015-12-24"
                    attachTo = "Private-Project"
                }
            )
        }

        @Test
        fun `private submission to public collection`() {
            val submission = tsv {
                line("Submission", "S-PUP0")
                line("AttachTo", "Public-Project")
                line("ReleaseDate", "2050-12-24")
                line("Title", "Private submission into public project")
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo("S-PUP0")).isEqualTo(
                submission("S-PUP0") {
                    title = "Private submission into public project"
                    releaseDate = "2050-12-24"
                    attachTo = "Public-Project"
                }
            )
        }

        @Test
        fun `no release date to public collection`() {
            val submission = tsv {
                line("Submission", "S-PUP1")
                line("AttachTo", "Public-Project")
                line("Title", "No Release Date To Public Project")
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo("S-PUP1")).isEqualTo(
                submission("S-PUP1") {
                    title = "No Release Date To Public Project"
                    attachTo = "Public-Project"
                }
            )
        }

        @Test
        fun `submit to collection with validator`() {
            val submission = tsv {
                line("Submission", "S-VLD0")
                line("AttachTo", "ValidatedCollection")
                line("Title", "A Validated Submission")
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(testCollectionValidator.validated).isTrue()
            assertThat(submissionRepository.getSimpleByAccNo("S-VLD0")).isEqualTo(
                submission("S-VLD0") {
                    title = "A Validated Submission"
                    attachTo = "ValidatedCollection"
                }
            )
        }

        @Test
        fun `submit to collection with failling validator`() {
            val submission = tsv {
                line("Submission", "S-FLC0")
                line("AttachTo", "FailCollection")
                line("Title", "A Fail Submission")
            }.toString()

            val exception = assertFailsWith<WebClientException> { webClient.submitSingle(submission, TSV) }
            assertThat(exception.message!!.contains("Testing failure"))
        }

        private fun setUpCollections() {
            val testProject = tsv {
                line("Submission", "Test-Project")
                line("AccNoTemplate", "!{S-TEST}")
                line()

                line("Project")
            }.toString()

            val privateProject = tsv {
                line("Submission", "Private-Project")
                line("AccNoTemplate", "!{S-PRP}")
                line()

                line("Project")
            }.toString()

            val publicProject = tsv {
                line("Submission", "Public-Project")
                line("AccNoTemplate", "!{S-PUP}")
                line("ReleaseDate", "2018-09-21")
                line()

                line("Project")
            }.toString()

            val validatedCollection = tsv {
                line("Submission", "ValidatedCollection")
                line("AccNoTemplate", "!{S-VLD}")
                line("CollectionValidator", "TestCollectionValidator")
                line()

                line("Project")
            }.toString()

            val failCollection = tsv {
                line("Submission", "FailCollection")
                line("AccNoTemplate", "!{S-FLC}")
                line("CollectionValidator", "FailCollectionValidator")
                line()

                line("Project")
            }.toString()

            assertThat(webClient.submitSingle(testProject, TSV)).isSuccessful()
            assertThat(webClient.submitSingle(publicProject, TSV)).isSuccessful()
            assertThat(webClient.submitSingle(privateProject, TSV)).isSuccessful()
            assertThat(webClient.submitSingle(failCollection, TSV)).isSuccessful()
            assertThat(webClient.submitSingle(validatedCollection, TSV)).isSuccessful()
        }
    }
}
