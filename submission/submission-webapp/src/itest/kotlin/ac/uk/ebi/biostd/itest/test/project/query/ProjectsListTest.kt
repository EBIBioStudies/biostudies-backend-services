package ac.uk.ebi.biostd.itest.test.project.query

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.DummyBaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.DefaultUser
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
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
import org.springframework.transaction.annotation.Transactional

@ExtendWith(TemporaryFolderExtension::class)
internal class ProjectsListTest() : DummyBaseIntegrationTest() {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class ExtCollectionListTest(
        @Autowired val userDataRepository: UserDataRepository,
        @Autowired val tagsDataRepository: AccessTagDataRepo,
        @Autowired val accessPermissionRepository: AccessPermissionRepository,
        @Autowired val securityTestService: SecurityTestService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var superUserWebClient: BioWebClient
        private lateinit var regularUserWebClient: BioWebClient

        @BeforeAll
        fun init() {
            securityTestService.deleteSuperUser()
            tagsDataRepository.deleteAll()
            accessPermissionRepository.deleteAll()

            securityTestService.registerUser(SuperUser)
            securityTestService.registerUser(RegularUser)
            securityTestService.registerUser(DefaultUser)

            superUserWebClient = getWebClient(serverPort, SuperUser)
            regularUserWebClient = getWebClient(serverPort, RegularUser)
            registerProjects()
        }

        @Test
        fun `list projects for super user`() {
            val projects = superUserWebClient.getCollections()

            assertThat(projects).hasSize(2)
            assertThat(projects.first().accno).isEqualTo("SampleProject")
            assertThat(projects.second().accno).isEqualTo("DefaultProject")
        }

        @Test
        fun `list projects for regular user`() {
            val projects = regularUserWebClient.getCollections()

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

            assertThat(superUserWebClient.submitSingle(sampleProject, TSV)).isSuccessful()
            assertThat(superUserWebClient.submitSingle(defaultProject, TSV)).isSuccessful()

            accessPermissionRepository.save(
                DbAccessPermission(
                    user = userDataRepository.getByEmailAndActive(DefaultUser.email, true),
                    accessTag = tagsDataRepository.getByName("DefaultProject"),
                    accessType = ATTACH
                )
            )
        }
    }
}
