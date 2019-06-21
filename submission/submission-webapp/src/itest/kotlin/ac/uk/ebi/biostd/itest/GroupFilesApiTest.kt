package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.GenericUser
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.security.integration.components.IGroupService
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

private const val GROUP_NAME = "Bio-test-group"

@ExtendWith(TemporaryFolderExtension::class)
internal class GroupFilesApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class GroupFilesApi(@Autowired val groupService: IGroupService) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(RegisterRequest(GenericUser.username, GenericUser.email, GenericUser.password))
            groupService.addUserInGroup(groupService.creatGroup(GROUP_NAME).name, "test@biostudies.com")
            webClient = securityClient.getAuthenticatedClient(GenericUser.email, GenericUser.password)
        }

        @Test
        fun `upload group file and retrieve in user folder`() {
            val file = tempFolder.createFile("FileList1.txt")

            webClient.uploadGroupFiles(GROUP_NAME, listOf(file))

            val files = webClient.listGroupFiles(GROUP_NAME)
            assertThat(files).hasSize(1)

            val resultFile = files.first()
            assertThat(resultFile.name).isEqualTo(file.name)
            assertThat(resultFile.type).isEqualTo(UserFileType.FILE)

            webClient.deleteGroupFile(GROUP_NAME, "FileList1.txt")
        }

        @Test
        fun `upload group file in directory and retrieve in user folder`() {
            val file = tempFolder.createFile("AnotherFile.txt")

            webClient.createGroupFolder(GROUP_NAME, "test_folder")
            webClient.uploadGroupFiles(GROUP_NAME, listOf(file), relativePath = "test_folder")

            val files = webClient.listGroupFiles(GROUP_NAME, relativePath = "test_folder")
            assertThat(files).hasSize(1)

            val resultFile = files.first()
            assertThat(resultFile.name).isEqualTo(file.name)
            assertThat(resultFile.type).isEqualTo(UserFileType.FILE)

            webClient.deleteGroupFile(GROUP_NAME, "test_folder")
        }
    }
}
