package ac.uk.ebi.biostd.itest.test.project.query

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.DefaultUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectsListTest(
    @Autowired val userDataRepository: UserDataRepository,
    @Autowired val tagsDataRepository: AccessTagDataRepo,
    @Autowired val accessPermissionRepository: AccessPermissionRepository,
    @Autowired val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {

    private lateinit var superUserWebClient: BioWebClient
    private lateinit var regularUserWebClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(ProjectUser)
        securityTestService.ensureUserRegistration(RegularUser)
        securityTestService.ensureUserRegistration(DefaultUser)

        superUserWebClient = getWebClient(serverPort, ProjectUser)
        regularUserWebClient = getWebClient(serverPort, RegularUser)
        registerProjects()
    }

    @Test
    fun `list projects for super user`() {
        val projects = superUserWebClient.getCollections()

        assertThat(projects).hasSizeGreaterThanOrEqualTo(2)
        assertThat(projects).anyMatch { it.accno == "SampleProject" }
        assertThat(projects).anyMatch { it.accno == "DefaultProject" }
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

    object ProjectUser : TestUser {
        override val username = "Project Super User"
        override val email = "project-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
    }

    object RegularUser : TestUser {
        override val username = "Regular Project User"
        override val email = "regular-project-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = false
    }
}
