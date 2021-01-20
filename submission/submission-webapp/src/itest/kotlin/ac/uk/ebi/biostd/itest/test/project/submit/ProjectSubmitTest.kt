package ac.uk.ebi.biostd.itest.test.project.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.Project
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
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
internal class ProjectSubmitTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class ProjectSubmitTest(
        @Autowired val securityTestService: SecurityTestService,
        @Autowired val tagsDataRepository: AccessTagDataRepo,
        @Autowired val submissionRepository: SubmissionQueryService,
        @Autowired val sequenceRepository: SequenceDataRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

        @Test
        fun `submit private project`() {
            val privateProject = tsv {
                line("Submission", "PrivateProject")
                line("Title", "A Private Project")
                line("AccNoTemplate", "!{S-PRP}")
                line()

                line("Project")
            }.toString()

            assertThat(webClient.submitSingle(privateProject, SubmissionFormat.TSV)).isSuccessful()

            val submittedProject = submissionRepository.getExtByAccNo("PrivateProject")
            assertThat(submittedProject.accNo).isEqualTo("PrivateProject")
            assertThat(submittedProject.title).isEqualTo("A Private Project")
            assertThat(submittedProject.status).isEqualTo(PROCESSED)

            assertThat(submittedProject.projects).hasSize(1)
            assertThat(submittedProject.projects.first().accNo).isEqualTo("PrivateProject")

            assertThat(tagsDataRepository.existsByName("PrivateProject")).isTrue()
            assertThat(sequenceRepository.existsByPrefix("S-PRP")).isTrue()
        }

        @Test
        fun `submit public project`() {
            val publicProject = tsv {
                line("Submission", "PublicProject")
                line("Title", "Public Project")
                line("AccNoTemplate", "!{S-PUP}")
                line("ReleaseDate", "2015-06-09")
                line()

                line("Project")
            }.toString()

            assertThat(webClient.submitSingle(publicProject, SubmissionFormat.TSV)).isSuccessful()

            val submittedProject = submissionRepository.getExtByAccNo("PublicProject")
            assertThat(submittedProject.accNo).isEqualTo("PublicProject")
            assertThat(submittedProject.title).isEqualTo("Public Project")
            assertThat(submittedProject.status).isEqualTo(PROCESSED)
            assertThat(submittedProject.projects).containsExactly(Project("PublicProject"))
            assertThat(tagsDataRepository.existsByName("PublicProject")).isTrue()
            assertThat(sequenceRepository.existsByPrefix("S-PUP")).isTrue()
        }

        @Test
        fun `submit duplicated accNo template`() {
            val aProject = tsv {
                line("Submission", "A-Project")
                line("AccNoTemplate", "!{S-APRJ}")
                line()

                line("Project")
            }.toString()

            val anotherProject = tsv {
                line("Submission", "Another-Project")
                line("AccNoTemplate", "!{S-APRJ}")
                line()

                line("Project")
            }.toString()

            assertThat(webClient.submitSingle(aProject, SubmissionFormat.TSV)).isSuccessful()
            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { webClient.submitSingle(anotherProject, SubmissionFormat.TSV) }
                .withMessageContaining("There is a project already using the accNo template 'S-APRJ'")
        }
    }
}
