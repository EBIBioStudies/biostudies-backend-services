package ac.uk.ebi.biostd.itest.test.files

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.io.ext.createFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Paths

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserFileApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(FilesUser)
        webClient = getWebClient(serverPort, FilesUser)
    }

    @Test
    fun `17-1 upload download delete file and retrieve in user root folder`() {
        val file = tempFolder.createFile("FileList1.txt", "An example content")
        webClient.uploadFiles(listOf(file), relativePath = "")

        val files = webClient.listUserFiles(relativePath = "")
        assertThat(files).hasSize(1)
        assertFile(files.first(), webClient.downloadFile(file.name, ""), file, "")

        webClient.deleteFile("FileList1.txt", "")
        assertThat(webClient.listUserFiles(relativePath = "")).isEmpty()
    }

    @Test
    fun `17-2 upload download delete file and retrieve in user folder`() {
        val file = tempFolder.createFile("FileList1.txt", "An example content")
        webClient.uploadFiles(listOf(file), relativePath = "test-folder")

        val files = webClient.listUserFiles(relativePath = "test-folder")
        assertThat(files).hasSize(1)
        assertFile(files.first(), webClient.downloadFile(file.name, "test-folder"), file, "test-folder")

        webClient.deleteFile("FileList1.txt", "test-folder")
        assertThat(webClient.listUserFiles(relativePath = "test-folder")).isEmpty()
        webClient.deleteFile("test-folder")
    }

    @Test
    fun `17-3 upload download delete file and retrieve in user folder with space`() {
        val file = tempFolder.createFile("FileList1.txt", "An example content")
        webClient.uploadFiles(listOf(file), relativePath = "test folder")

        val files = webClient.listUserFiles(relativePath = "test folder")
        assertThat(files).hasSize(1)
        assertFile(files.first(), webClient.downloadFile(file.name, "test folder"), file, "test folder")

        webClient.deleteFile("FileList1.txt", "test folder")
        assertThat(webClient.listUserFiles(relativePath = "test folder")).isEmpty()
        webClient.deleteFile("test folder")
    }

    private fun assertFile(resultFile: UserFile, downloadFile: File, file: File, relativePath: String) {
        assertThat(resultFile.name).isEqualTo(file.name)
        assertThat(resultFile.type).isEqualTo(UserFileType.FILE)
        assertThat(resultFile.size).isEqualTo(file.length())
        assertThat(resultFile.path).isEqualTo(Paths.get("user").resolve(relativePath).toString())
        assertThat(file).hasContent(downloadFile.readText())
    }

    object FilesUser : TestUser {
        override val username = "Files User"
        override val email = "files-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true

        override fun asRegisterRequest() = RegisterRequest(username, email, password)
    }
}
