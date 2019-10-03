package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.model.AccessPermission
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
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
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
internal class ProjectsApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, TestConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class ProjectListTest(
        @Autowired val userDataRepository: UserDataRepository,
        @Autowired val tagsDataRepository: TagsDataRepository,
        @Autowired val submissionRepository: SubmissionRepository,
        @Autowired val accessPermissionRepository: AccessPermissionRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(SuperUser.asRegisterRequest())

            webClient = securityClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
            registerProject()
        }

        private fun registerProject() {
            val project = tsv {
                line("Submission", "SampleProject")
                line("Title", "Sample Project")
                line()

                line("Project")
            }

            webClient.submitProject(tempFolder.createFile("project.tsv", project.toString()))

            // TODO add operation to provide permissions
            accessPermissionRepository.save(AccessPermission(
                user = userDataRepository.findByEmailAndActive(SuperUser.email, true).get(),
                accessTag = tagsDataRepository.findByName("SampleProject"),
                accessType = AccessType.ATTACH))
        }

        @Test
        fun `submit project`() {
            val project = tsv {
                line("Submission", "AProject")
                line("Title", "A Project")
                line()

                line("Project")
            }

            val response = webClient.submitProject(tempFolder.createFile("a-project.tsv", project.toString()))
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val submittedProject = submissionRepository.getExtendedByAccNo("AProject")
            assertThat(submittedProject.accNo).isEqualTo("AProject")
            assertThat(submittedProject.title).isEqualTo("A Project")

            assertThat(submittedProject.accessTags).hasSize(1)
            assertThat(submittedProject.accessTags.first()).isEqualTo("AProject")

            assertThat(tagsDataRepository.existsByName("AProject")).isTrue()
        }

        @Test
        fun `get projects`() {
            val projects = webClient.getProjects()
            assertThat(projects).hasSize(1)

            val project = projects.first()
            assertThat(project.accno).isEqualTo("SampleProject")
            assertThat(project.title).isEqualTo("Sample Project")
        }
    }
}
