package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.GenericUser
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.api.security.RegisterRequest
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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class)
internal class UserFileApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class FilesTest {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(RegisterRequest(
                GenericUser.email,
                GenericUser.username,
                GenericUser.password
            ))
            webClient = securityClient.getAuthenticatedClient(GenericUser.username, GenericUser.password)
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
            assertThat(resultFile.path).isEqualTo(Paths.get("user").resolve(relativePath).resolve(file.name).toString())
            assertThat(file).hasContent(downloadFile.readText())
        }

    }
}
