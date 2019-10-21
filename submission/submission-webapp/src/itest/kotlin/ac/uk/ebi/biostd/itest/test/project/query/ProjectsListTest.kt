package ac.uk.ebi.biostd.itest.test.project.query

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.model.AccessPermission
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
internal class ProjectsListTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class ProjectListTest(
        @Autowired val userDataRepository: UserDataRepository,
        @Autowired val tagsDataRepository: TagsDataRepository,
        @Autowired val accessPermissionRepository: AccessPermissionRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
            registerProject()
        }

        @Test
        fun `get projects`() {
            val projects = webClient.getProjects()
            assertThat(projects).hasSize(1)

            val project = projects.first()
            assertThat(project.accno).isEqualTo("SampleProject")
            assertThat(project.title).isEqualTo("Sample Project")
        }

        private fun registerProject() {
            val project = tsv {
                line("Submission", "SampleProject")
                line("Title", "Sample Project")
                line()

                line("Project")
            }

            val projectFile = tempFolder.createFile("project.tsv", project.toString())
            assertThat(webClient.submitProject(projectFile)).isSuccessful()

            // TODO add operation to provide permissions
            accessPermissionRepository.save(AccessPermission(
                user = userDataRepository.findByEmailAndActive(SuperUser.email, true).get(),
                accessTag = tagsDataRepository.findByName("SampleProject"),
                accessType = AccessType.ATTACH))
        }
    }
}
