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
        fun `no release date to private project`() {
            val submission = tsv {
                line("Submission")
                line("AttachTo", "Private-Project")
                line("Title", "No Release Date To Private Project")
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-PRP0")).isEqualTo(
                submission("S-PRP0") {
                    attachTo = "Private-Project"
                    title = "No Release Date To Private Project"
                    accessTags = mutableListOf("Private-Project")
                }
            )
        }

        @Test
        fun `public submission to private project`() {
            val submission = tsv {
                line("Submission")
                line("AttachTo", "Private-Project")
                line("ReleaseDate", "2018-09-21")
                line("Title", "Public submission into private project")
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-PRP1")).isEqualTo(
                submission("S-PRP1") {
                    attachTo = "Private-Project"
                    title = "Public submission into private project"
                    accessTags = mutableListOf("Private-Project")
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
                    attachTo = "Public-Project"
                    title = "Private submission into public project"
                    releaseDate = "2050-12-24"
                    accessTags = mutableListOf("Public-Project")
                }
            )
        }

        @Test
        fun `public submission with past release date to public project`() {
            val submission = tsv {
                line("Submission", "S-PUP1")
                line("AttachTo", "Public-Project")
                line("ReleaseDate", "2016-06-09")
                line("Title", "Public Submission With Past Release Date")
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-PUP1")).isEqualTo(
                submission("S-PUP1") {
                    attachTo = "Public-Project"
                    title = "Public Submission With Past Release Date"
                    releaseDate = "2018-09-21"
                    accessTags = mutableListOf("Public-Project", "Public")
                }
            )
        }

        @Test
        fun `public submission with future release date to public project`() {
            val submission = tsv {
                line("Submission", "S-PUP2")
                line("AttachTo", "Public-Project")
                line("ReleaseDate", "2019-06-09")
                line("Title", "Public Submission With Future Release Date")
            }.toString()

            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-PUP2")).isEqualTo(
                submission("S-PUP2") {
                    attachTo = "Public-Project"
                    title = "Public Submission With Future Release Date"
                    releaseDate = "2019-06-09"
                    accessTags = mutableListOf("Public-Project", "Public")
                }
            )
        }

        private fun setUpProjects() {
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

            // TODO these won't need to be files once the refactor to the model is merged
            assertThat(webClient.submitProject(tempFolder.createFile("public.tsv", publicProject))).isSuccessful()
            assertThat(webClient.submitProject(tempFolder.createFile("private.tsv", privateProject))).isSuccessful()
        }
    }
}
