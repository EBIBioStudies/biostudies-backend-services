package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
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

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionToProjectsTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmitToProjectTest(@Autowired val submissionRepository: SubmissionRepository) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
            setUpProjects()
        }

        @Test
        fun `accNo generation from project template`() {
            val submission = tsv {
                line("Submission")
                line("AttachTo", "Test-Project")
                line("Title", "AccNo Generation Test")
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
            val expected = submission("S-TEST0") {
                title = "AccNo Generation Test"
                attachTo = "Test-Project"
            }
            assertThat(submissionRepository.getByAccNo("S-TEST0")).isEqualTo(expected)
        }

        @Test
        fun `direct submission overriding project`() {
            val submissionFile = tempFolder.createFile(
                "submission.tsv",
                tsv {
                    line("Submission", "S-TEST1")
                    line("AttachTo", "Test-Project")
                    line("Title", "Overridden Project")
                }.toString())

            assertThat(webClient.submitSingle(
                submissionFile, emptyList(), singletonMap("AttachTo", "Public-Project"))).isSuccessful()

            assertThat(submissionRepository.getByAccNo("S-TEST1")).isEqualTo(
                submission("S-TEST1") {
                    title = "Overridden Project"
                    attachTo = "Public-Project"
                }
            )
        }

        @Test
        fun `no release date to private project`() {
            val submission = tsv {
                line("Submission", "S-PRP0")
                line("AttachTo", "Private-Project")
                line("Title", "No Release Date To Private Project")
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-PRP0")).isEqualTo(
                submission("S-PRP0") {
                    title = "No Release Date To Private Project"
                    attachTo = "Private-Project"
                }
            )
        }

        @Test
        fun `public submission to private project`() {
            val submission = tsv {
                line("Submission", "S-PRP1")
                line("AttachTo", "Private-Project")
                line("ReleaseDate", "2015-12-24")
                line("Title", "Public Submission To Private Project")
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-PRP1")).isEqualTo(
                submission("S-PRP1") {
                    title = "Public Submission To Private Project"
                    releaseDate = "2015-12-24"
                    attachTo = "Private-Project"
                }
            )
        }

        @Test
        fun `private submission to public project`() {
            val submission = tsv {
                line("Submission", "S-PUP0")
                line("AttachTo", "Public-Project")
                line("ReleaseDate", "2050-12-24")
                line("Title", "Private submission into public project")
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-PUP0")).isEqualTo(
                submission("S-PUP0") {
                    title = "Private submission into public project"
                    releaseDate = "2050-12-24"
                    attachTo = "Public-Project"
                }
            )
        }

        @Test
        fun `no release date to public project`() {
            val submission = tsv {
                line("Submission", "S-PUP1")
                line("AttachTo", "Public-Project")
                line("Title", "No Release Date To Public Project")
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-PUP1")).isEqualTo(
                submission("S-PUP1") {
                    title = "No Release Date To Public Project"
                    attachTo = "Public-Project"
                }
            )
        }

        private fun setUpProjects() {
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

            assertThat(webClient.submitSingle(testProject, SubmissionFormat.TSV)).isSuccessful()
            assertThat(webClient.submitSingle(publicProject, SubmissionFormat.TSV)).isSuccessful()
            assertThat(webClient.submitSingle(privateProject, SubmissionFormat.TSV)).isSuccessful()
        }
    }
}
