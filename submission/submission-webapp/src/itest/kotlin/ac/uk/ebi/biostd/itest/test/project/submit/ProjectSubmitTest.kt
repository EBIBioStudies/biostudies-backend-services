package ac.uk.ebi.biostd.itest.test.project.submit

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
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
internal class ProjectSubmitTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class ProjectSubmitTest(
        @Autowired val tagsDataRepository: TagsDataRepository,
        @Autowired val submissionRepository: SubmissionRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
        }

        @Test
        fun `submit project`() {
            val project = tsv {
                line("Submission", "AProject")
                line("Title", "A Project")
                line()

                line("Project")
            }

            val projectFile = tempFolder.createFile("a-project.tsv", project.toString())
            assertThat(webClient.submitProject(projectFile)).isSuccessful()

            val submittedProject = submissionRepository.getExtendedByAccNo("AProject")
            assertThat(submittedProject.accNo).isEqualTo("AProject")
            assertThat(submittedProject.title).isEqualTo("A Project")

            assertThat(submittedProject.accessTags).hasSize(1)
            assertThat(submittedProject.accessTags.first()).isEqualTo("AProject")

            assertThat(tagsDataRepository.existsByName("AProject")).isTrue()
        }
    }
}
