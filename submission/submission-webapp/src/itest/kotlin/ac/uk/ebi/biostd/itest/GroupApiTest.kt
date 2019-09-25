package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

private const val GROUP_NAME = "Bio-test-group"
private const val GROUP_DESC = "Bio-test-group description"

@ExtendWith(TemporaryFolderExtension::class)
internal class GroupsApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class GroupsApi(@Autowired val groupService: IGroupService) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(RegisterRequest(SuperUser.username, SuperUser.email, SuperUser.password))
            groupService.addUserInGroup(groupService.createGroup(GROUP_NAME, GROUP_DESC).name, SuperUser.email)
            webClient = securityClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
        }

        @BeforeEach
        fun beforeEach() {
            tempFolder.clean()
        }

        @Test
        fun `get user groups`() {
            val groups = webClient.getGroups()

            assertThat(groups).hasSize(1)
            assertThat(groups.first()).satisfies {
                assertThat(it.description).isEqualTo(GROUP_DESC)
                assertThat(it.name).isEqualTo(GROUP_NAME)
            }
        }
    }
}
