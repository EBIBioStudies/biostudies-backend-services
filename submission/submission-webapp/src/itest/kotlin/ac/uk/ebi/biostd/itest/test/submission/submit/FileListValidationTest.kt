package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.io.ext.createFile
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(PersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileListValidationTest(@Autowired private val securityTestService: SecurityTestService, @LocalServerPort val serverPort: Int) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    @Test
    fun `empty file list`() {
        val fileList = tempFolder.createFile("FileList.json")

        webClient.uploadFile(fileList)

        val exception = Assertions.assertThrows(WebClientException::class.java) {
            webClient.validateFileList(fileList.name)
        }

        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception).hasMessageContaining("Expected content to be an array")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `unsupported file list format`() {
        val fileList = tempFolder.createFile("image.jpg")

        webClient.uploadFile(fileList)

        val exception = Assertions.assertThrows(WebClientException::class.java) {
            webClient.validateFileList(fileList.name)
        }

        assertThat(exception.statusCode).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        assertThat(exception).hasMessageContaining("Unsupported page tab format image.jpg")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `valid file list`() {
        val fileListFile = tempFolder.createFile("Plate1.tif")
        val fileList = tempFolder.createFile("FileList.json", getFileListContent().toString())

        webClient.uploadFiles(listOf(fileListFile, fileList))

        Assertions.assertDoesNotThrow {
            webClient.validateFileList(fileList.name)
        }

        webClient.deleteFile(fileListFile.name)
        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `file list with missing files`() {
        val fileList = tempFolder.createFile("FileList.json", getFileListContent().toString())

        webClient.uploadFile(fileList)

        val exception = Assertions.assertThrows(WebClientException::class.java) {
            webClient.validateFileList(fileList.name)
        }

        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception).hasMessageContaining("File not uploaded: Plate1.tif")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `empty file list on behalf another user`() {
        securityTestService.ensureUserRegistration(RegularUser)

        val fileList = tempFolder.createFile("FileList.json")
        webClient.uploadFile(fileList)

        val onBehalfClient = SecurityWebClient.create("http://localhost:$serverPort")
            .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

        val exception = Assertions.assertThrows(WebClientException::class.java) {
            onBehalfClient.validateFileList(fileList.name)
        }

        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception).hasMessageContaining("Expected content to be an array")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `valid file list on behalf another user`() {
        val fileListFile = tempFolder.createFile("Plate1.tif")
        val fileList = tempFolder.createFile("FileList.json", getFileListContent().toString())

        webClient.uploadFiles(listOf(fileListFile, fileList))

        Assertions.assertDoesNotThrow {
            webClient.validateFileList(fileList.name)
        }

        webClient.deleteFile(fileListFile.name)
        webClient.deleteFile(fileList.name)
    }

    private fun getFileListContent() = jsonArray(
        jsonObj {
            "path" to "Plate1.tif"
            "size" to 290
            "type" to "file"
        }
    )
}
