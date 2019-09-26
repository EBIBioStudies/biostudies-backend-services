package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.SubmissionTypes
import ac.uk.ebi.biostd.persistence.model.AccessPermission
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.section
import ebi.ac.uk.model.extensions.addAccessTag
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.persistence.PersistenceContext
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
internal class ProjectsApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, TestConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class ProjectListTest(
        @Autowired val submissionRepository: SubmissionRepository,
        @Autowired val tagsDataRepository: TagsDataRepository,
        @Autowired val accessPermissionRepository: AccessPermissionRepository,
        @Autowired val userDataRepository: UserDataRepository,
        @Autowired val persistenceContext: PersistenceContext
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        private val projectAccNo = "SampleProject1"

        private lateinit var accessTag: AccessTag

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(SuperUser.asRegisterRequest())

            webClient = securityClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
            accessTag = AccessTag(name = projectAccNo)
            tagsDataRepository.save(accessTag)
            accessPermissionRepository.save(AccessPermission(
                user = userDataRepository.findByEmailAndActive(SuperUser.email, true).get(),
                accessTag = accessTag,
                accessType = AccessType.ATTACH))
        }

        @Test
        fun `get projects`() {
            val submission = submission(projectAccNo) {
                title = "Sample Project"
                accessTags = mutableListOf(projectAccNo)
                section(SubmissionTypes.Project.value) {}
            }
            webClient.submitSingle(submission, SubmissionFormat.JSON)

            val savedSubmission = submissionRepository.getExtendedByAccNo(projectAccNo)
            savedSubmission.addAccessTag(projectAccNo) // TODO: Remove when access tag has been handled automatically
            persistenceContext.saveSubmission(savedSubmission)

            val projects = webClient.getProjects()
            assertThat(projects).isNotEmpty
            assertThat(projects.first().accno).isEqualTo(projectAccNo)
        }
    }
}
