package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.GenericUser
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.test.clean
import ebi.ac.uk.test.createFile
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
import java.io.File
import java.nio.file.Paths

private const val GROUP_NAME = "Bio-test-group"
private const val GROUP_DESC = "Bio-test-group description"

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
            securityClient.registerUser(RegisterRequest(GenericUser.name, GenericUser.email, GenericUser.password))
            groupService.addUserInGroup(groupService.createGroup(GROUP_NAME, GROUP_DESC).name, GenericUser.email)
            webClient = securityClient.getAuthenticatedClient(GenericUser.email, GenericUser.password)
        }

        @BeforeEach
        fun beforeEach() {
            tempFolder.clean()
        }

        @Test
        fun `upload|download|delete file and retrieve in user root folder`() {
            testUserFilesGroup()
        }

        @Test
        fun `upload|download|delete file and retrieve in user folder`() {
            testUserFilesGroup("test-folder")
        }

        private fun testUserFilesGroup(path: String = "") {
            val file = tempFolder.createFile("FileList1.txt", "An example content")
            webClient.uploadGroupFiles(GROUP_NAME, listOf(file), path)

            val files = webClient.listGroupFiles(GROUP_NAME, path)
            assertThat(files).hasSize(1)
            assertFile(files.first(), webClient.downloadGroupFile(GROUP_NAME, file.name, path), file, path)

            webClient.deleteGroupFile(GROUP_NAME, "FileList1.txt", path)
            assertThat(webClient.listGroupFiles(GROUP_NAME, path)).isEmpty()
        }

        private fun assertFile(resultFile: UserFile, downloadFile: File, file: File, path: String) {
            assertThat(resultFile.name).isEqualTo(file.name)
            assertThat(resultFile.type).isEqualTo(UserFileType.FILE)
            assertThat(resultFile.path).isEqualTo(Paths.get("groups").resolve(GROUP_NAME).resolve(path).toString())
            assertThat(resultFile.size).isEqualTo(file.length())
            assertThat(file).hasContent(downloadFile.readText())
        }
    }
}
