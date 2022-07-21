package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.enableFire
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.ac.ebi.fire.client.integration.web.FireClient

@Import(PersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileListValidationTest(
    @Autowired private val fireClient: FireClient,
    @Autowired private val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(RegUser)
        webClient = getWebClient(serverPort, RegUser)
    }

    @Test
    fun `empty file list`() {
        val fileList = tempFolder.createFile("FileList.json")

        webClient.uploadFile(fileList)

        val exception = assertThrows(WebClientException::class.java) { webClient.validateFileList(fileList.name) }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception).hasMessageContaining("Expected content to be an array")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `unsupported file list format`() {
        val fileList = tempFolder.createFile("image.jpg")

        webClient.uploadFile(fileList)

        val exception = assertThrows(WebClientException::class.java) {
            webClient.validateFileList(fileList.name)
        }

        assertThat(exception.statusCode).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        assertThat(exception).hasMessageContaining("Unsupported page tab format image.jpg")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `valid file list`() {
        val previousVersion = tsv {
            line("Submission", "S-FLV123")
            line()

            line("Study")
            line()

            line("File", "Plate2.tif")
            line()
        }.toString()

        val file1 = tempFolder.createFile("Plate1.tif", "content-1")
        val file2 = tempFolder.createFile("Plate2.tif", "content-2")
        val fileListContent = tsv {
            line("Files", "Resource")
            line("Plate1.tif", "USER_SPACE")
            line("Plate2.tif", "SUBMISSION")
            line()
        }.toString()

        val fileList = tempFolder.createFile("valid-file-list.tsv", fileListContent)

        webClient.uploadFiles(listOf(file1, fileList))
        webClient.submitSingle(previousVersion, TSV, SubmissionFilesConfig(listOf(file2)))

        webClient.validateFileList(fileList.name, "S-FLV123")

        webClient.deleteFile(file1.name)
        webClient.deleteFile(fileList.name)
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `valid file list including a file from FIRE`() {
        val previousVersion = tsv {
            line("Submission", "S-FLV124")
            line()

            line("Study")
            line()

            line("File", "Plate5.tif")
            line()
        }.toString()

        val file3 = tempFolder.createFile("Plate3.tif", "content-3")
        val file4 = tempFolder.createFile("Plate4.tif", "content-4")
        val file5 = tempFolder.createFile("Plate5.tif", "content-5")
        val file3Md5 = file3.md5()
        val file4Md5 = file4.md5()
        val file5Md5 = file5.md5()

        val fileListContent = tsv {
            line("Files", "Resource", "md5")
            line("Plate3.tif", "USER_SPACE", file3Md5)
            line("Plate4.tif", "FIRE", file4Md5)
            line("Plate5.tif", "SUBMISSION", file5Md5)
            line()
        }.toString()

        val fileList = tempFolder.createFile("fire-valid-file-list.tsv", fileListContent)

        fireClient.save(file4, file4Md5, file4.size())
        webClient.uploadFiles(listOf(file3, fileList))
        webClient.submitSingle(previousVersion, TSV, SubmissionFilesConfig(listOf(file5)))

        webClient.validateFileList(fileList.name, "S-FLV124")

        webClient.deleteFile(file3.name)
        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `file list with missing files`() {
        val fileList = tempFolder.createFile("FileList.json", getFileListContent().toString())

        webClient.uploadFile(fileList)

        val exception = assertThrows(WebClientException::class.java) { webClient.validateFileList(fileList.name) }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        val expectedNfsError = jsonObj {
            "log" to jsonObj {
                "level" to "ERROR"
                "message" to """
                        The following files could not be found:
                          - Plate1.tif
                        List of available sources:
                          - biostudies-mgmt-filelist-v@ebi.ac.uk user files
                """.trimIndent()
                "subnodes" to jsonArray()
            }
            "status" to "FAIL"
        }
        val expectedFireError = jsonObj {
            "log" to jsonObj {
                "level" to "ERROR"
                "message" to """
                        The following files could not be found:
                          - Plate1.tif
                        List of available sources:
                          - biostudies-mgmt-filelist-v@ebi.ac.uk user files
                          - EBI internal files Archive
                """.trimIndent()
                "subnodes" to jsonArray()
            }
            "status" to "FAIL"
        }
        val expectedError = if (enableFire) expectedFireError else expectedNfsError

        assertEquals(expectedError.toString(), exception.message, JSONCompareMode.LENIENT)
        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `empty file list on behalf another user`() {
        securityTestService.ensureUserRegistration(RegularUser)

        val fileList = tempFolder.createFile("FileList.json")
        webClient.uploadFile(fileList)

        val onBehalfClient = SecurityWebClient.create("http://localhost:$serverPort")
            .getAuthenticatedClient(RegUser.email, RegUser.password, RegularUser.email)

        val exception = assertThrows(WebClientException::class.java) { onBehalfClient.validateFileList(fileList.name) }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception).hasMessageContaining("Expected content to be an array")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `valid file list on behalf another user`() {
        val fileListFile = tempFolder.createFile("Plate1.tif")
        val fileList = tempFolder.createFile("FileList.json", getFileListContent().toString())

        webClient.uploadFiles(listOf(fileListFile, fileList))
        webClient.validateFileList(fileList.name)

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

    object RegUser : TestUser {
        override val username = "User File List Validation"
        override val email = "biostudies-mgmt-filelist-v@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
    }
}
