package ac.uk.ebi.biostd.itest.test.project.query

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.DefaultUser
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.model.AccessPermission
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.util.collections.second
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
internal class ProjectsListTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class ProjectListTest(
        @Autowired val userDataRepository: UserDataRepository,
        @Autowired val tagsDataRepository: AccessTagDataRepo,
        @Autowired val accessPermissionRepository: AccessPermissionRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var superUserWebClient: BioWebClient
        private lateinit var regularUserWebClient: BioWebClient

        @BeforeAll
        fun init() {
            superUserWebClient = getWebClient(serverPort, SuperUser)
            regularUserWebClient = getWebClient(serverPort, RegularUser)
            createUser(DefaultUser, serverPort)
            registerProjects()
        }

        @Test
        fun `list projects for super user`() {
            val projects = superUserWebClient.getProjects()

            assertThat(projects).hasSize(2)
            assertThat(projects.first().accno).isEqualTo("SampleProject")
            assertThat(projects.second().accno).isEqualTo("DefaultProject")
        }

        @Test
        fun `list projects for regular user`() {
            val projects = regularUserWebClient.getProjects()

            assertThat(projects).hasSize(1)
            assertThat(projects.first().accno).isEqualTo("DefaultProject")
        }

        private fun registerProjects() {
            val sampleProject = tsv {
                line("Submission", "SampleProject")
                line("AccNoTemplate", "!{S-SAMP}")
                line()

                line("Project")
            }.toString()

            val defaultProject = tsv {
                line("Submission", "DefaultProject")
                line("AccNoTemplate", "!{S-DFLT}")
                line()

                line("Project")
            }.toString()

            assertThat(superUserWebClient.submitSingle(sampleProject, SubmissionFormat.TSV)).isSuccessful()
            assertThat(superUserWebClient.submitSingle(defaultProject, SubmissionFormat.TSV)).isSuccessful()

            accessPermissionRepository.save(AccessPermission(
                user = userDataRepository.findByEmailAndActive(DefaultUser.email, true).get(),
                accessTag = tagsDataRepository.findByName("DefaultProject"),
                accessType = AccessType.ATTACH))
        }
    }
}
