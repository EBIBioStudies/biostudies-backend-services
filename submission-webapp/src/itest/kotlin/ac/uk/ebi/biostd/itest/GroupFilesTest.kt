package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.security.integration.model.SignUpRequest
import ebi.ac.uk.security.service.GroupService
import ebi.ac.uk.security.service.SecurityService
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import

private const val GROUP_NAME = "Bio-test-group"

@ExtendWith(TemporaryFolderExtension::class)
internal class GroupFilesTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {

    @Nested
    @TestInstance(PER_CLASS)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class GroupFilesApi {

        @LocalServerPort
        private var serverPort: Int = 0

        @Autowired
        private lateinit var securityService: SecurityService

        @Autowired
        private lateinit var groupService: GroupService

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityService.registerUser(SignUpRequest("test@biostudies.com", "jhon_doe", "12345"))
            webClient = BioWebClient.create("http://localhost:$serverPort", securityService.login("jhon_doe", "12345"))
            groupService.addUserInGroup(groupService.creatGroup(GROUP_NAME).name, "test@biostudies.com")
        }

        @Test
        fun `upload group file and retrieve in user folder`() {
            val file = tempFolder.createFile("LibraryFile1.txt")

            webClient.uploadGroupFiles(GROUP_NAME, listOf(file))

            val files = webClient.listGroupFiles(GROUP_NAME)
            Assertions.assertThat(files).hasSize(1)

            val resultFile = files.first()
            Assertions.assertThat(resultFile.name).isEqualTo(file.name)
            Assertions.assertThat(resultFile.type).isEqualTo(UserFileType.FILE)

            webClient.deleteGroupFile(GROUP_NAME, "LibraryFile1.txt")
        }

        @Test
        fun `upload group file in directory and retrieve in user folder`() {
            val file = tempFolder.createFile("AnotherFile.txt")

            webClient.createGroupFolder(GROUP_NAME, "test_folder")
            webClient.uploadGroupFiles(GROUP_NAME, listOf(file), relativePath = "test_folder")

            val files = webClient.listGroupFiles(GROUP_NAME, relativePath = "test_folder")
            Assertions.assertThat(files).hasSize(1)

            val resultFile = files.first()
            Assertions.assertThat(resultFile.name).isEqualTo(file.name)
            Assertions.assertThat(resultFile.type).isEqualTo(UserFileType.FILE)

            webClient.deleteGroupFile(GROUP_NAME, "test_folder")
        }
    }
}
