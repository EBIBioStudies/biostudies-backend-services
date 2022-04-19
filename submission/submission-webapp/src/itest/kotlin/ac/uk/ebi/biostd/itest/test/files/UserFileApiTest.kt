package ac.uk.ebi.biostd.itest.test.files

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.getWebClient
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType
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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class)
internal class UserFileApiTest(
    private val localFolder: TemporaryFolder
) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class FilesTest(
        @Autowired val securityTestService: SecurityTestService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0
        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityTestService.deleteSuperUser()

            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

        @BeforeEach
        fun beforeEach() {
            localFolder.clean()
        }

        @Test
        fun `upload download delete file and retrieve in user root folder`() {
            testUserFilesGroup()
        }

        @Test
        fun `upload download delete file and retrieve in user folder`() {
            testUserFilesGroup("test-folder")
        }

        @Test
        fun `upload download delete file and retrieve in user folder with space`() {
            testUserFilesGroup("test folder")
        }

        private fun testUserFilesGroup(path: String = "") {
            val file = localFolder.createFile("FileList1.txt", "An example content")
            webClient.uploadFiles(listOf(file), relativePath = path)

            val files = webClient.listUserFiles(relativePath = path)
            assertThat(files).hasSize(1)
            assertFile(files.first(), webClient.downloadFile(file.name, path), file, path)

            webClient.deleteFile("FileList1.txt", path)
            assertThat(webClient.listUserFiles(relativePath = path)).isEmpty()
        }

        private fun assertFile(resultFile: UserFile, downloadFile: File, file: File, relativePath: String) {
            assertThat(resultFile.name).isEqualTo(file.name)
            assertThat(resultFile.type).isEqualTo(UserFileType.FILE)
            assertThat(resultFile.size).isEqualTo(file.length())
            assertThat(resultFile.path).isEqualTo(Paths.get("user").resolve(relativePath).toString())
            assertThat(file).hasContent(downloadFile.readText())
        }
    }
}
