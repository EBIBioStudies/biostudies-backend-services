package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
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
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.ac.ebi.fire.client.integration.web.FireClient

@Import(FilePersistenceConfig::class)
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
    fun `blank file list`() {
        val fileList = tempFolder.createFile("BlankFileList.json")

        webClient.uploadFile(fileList)

        val exception = assertThrows(WebClientException::class.java) { webClient.validateFileList(fileList.name) }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception).hasMessageContaining("Expected content to be an array")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `empty file list`() {
        val fileList = tempFolder.createFile("EmptyFileList.tsv", "Files\tAttr1")

        webClient.uploadFile(fileList)

        val exception = assertThrows(WebClientException::class.java) { webClient.validateFileList(fileList.name) }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception).hasMessageContaining("A file list should contain at least one file")

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

            line("File", "File2.tif")
            line()
        }.toString()

        val file1 = tempFolder.createFile("File1.tif", "content-1")
        val file2 = tempFolder.createFile("File2.tif", "content-2")
        val fileListContent = tsv {
            line("Files")
            line("File1.tif")
            line("File2.tif")
            line()
        }.toString()

        val fileList = tempFolder.createFile("ValidFileList.tsv", fileListContent)

        webClient.uploadFiles(listOf(file1, fileList))
        webClient.submitSingle(previousVersion, TSV, SubmissionFilesConfig(listOf(file2), storageMode))

        webClient.validateFileList(fileList.name, accNo = "S-FLV123")

        webClient.deleteFile(file1.name)
        webClient.deleteFile(fileList.name)

        file1.delete()
        file2.delete()
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `valid file list including a file from FIRE`() {
        val previousVersion = tsv {
            line("Submission", "S-FLV124")
            line()

            line("Study")
            line()

            line("File", "File3.tif")
            line()
        }.toString()

        val file1 = tempFolder.createFile("File1.tif", "content-1")
        val file2 = tempFolder.createFile("File2.tif", "content-2")
        val file3 = tempFolder.createFile("File3.tif", "content-3")
        val file1Md5 = file1.md5()
        val file2Md5 = file2.md5()
        val file3Md5 = file3.md5()

        val fileListContent = tsv {
            line("Files", "dbMd5")
            line("File1.tif", file1Md5)
            line("File2.tif", file2Md5)
            line("File3.tif", file3Md5)
            line()
        }.toString()

        val fileList = tempFolder.createFile("FireValidFileList.tsv", fileListContent)

        fireClient.save(file2, file2Md5, file2.size())
        webClient.uploadFiles(listOf(file1, fileList))
        webClient.submitSingle(previousVersion, TSV, SubmissionFilesConfig(listOf(file3), storageMode))

        webClient.validateFileList(fileList.name, accNo = "S-FLV124")

        webClient.deleteFile(file1.name)
        webClient.deleteFile(fileList.name)

        file1.delete()
        file2.delete()
        file3.delete()
    }

    @Test
    fun `valid file list with root path`() {
        val file = tempFolder.createFile("File1.tif", "content-1")
        val fileListContent = tsv {
            line("Files")
            line("File1.tif")
            line()
        }.toString()

        val fileList = tempFolder.createFile("RootPathFileList.tsv", fileListContent)

        webClient.uploadFiles(listOf(file, fileList), "root-path")

        webClient.validateFileList(fileList.name, rootPath = "root-path")

        webClient.deleteFile(file.name)
        webClient.deleteFile(fileList.name)

        file.delete()
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `file list with missing files on NFS mode`() {
        val fileList = tempFolder.createFile("InvalidNfsFileList.json", FILE_LIST_CONTENT)

        webClient.uploadFile(fileList)

        val exception = assertThrows(WebClientException::class.java) { webClient.validateFileList(fileList.name) }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        val expectedError = jsonObj {
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

        assertEquals(expectedError.toString(), exception.message, JSONCompareMode.LENIENT)
        webClient.deleteFile(fileList.name)
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `file list with missing files on FIRE mode`() {
        val fileList = tempFolder.createFile("InvalidFireFileList.json", FILE_LIST_CONTENT)

        webClient.uploadFile(fileList)

        val exception = assertThrows(WebClientException::class.java) { webClient.validateFileList(fileList.name) }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        val expectedError = jsonObj {
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

        assertEquals(expectedError.toString(), exception.message, JSONCompareMode.LENIENT)
        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `blank file list on behalf another user`() {
        securityTestService.ensureUserRegistration(RegularUser)

        val fileList = tempFolder.createFile("BlankOnBehalfFileList.json")
        webClient.uploadFile(fileList)

        val onBehalfClient = SecurityWebClient.create("http://localhost:$serverPort")
            .getAuthenticatedClient(RegUser.email, RegUser.password, RegularUser.email)

        val exception = assertThrows(WebClientException::class.java) { onBehalfClient.validateFileList(fileList.name) }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception).hasMessageContaining("Expected content to be an array")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `empty file list on behalf another user`() {
        securityTestService.ensureUserRegistration(RegularUser)

        val fileList = tempFolder.createFile("EmptyOnBehalfFileList.tsv", "Files\tAttr1")
        webClient.uploadFile(fileList)

        val onBehalfClient = SecurityWebClient.create("http://localhost:$serverPort")
            .getAuthenticatedClient(RegUser.email, RegUser.password, RegularUser.email)

        val exception = assertThrows(WebClientException::class.java) { onBehalfClient.validateFileList(fileList.name) }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception).hasMessageContaining("A file list should contain at least one file")

        webClient.deleteFile(fileList.name)
    }

    @Test
    fun `valid file list on behalf another user`() {
        val fileListFile = tempFolder.createFile("Plate1.tif")
        val fileList = tempFolder.createFile("ValidOnBehalfFileList.json", FILE_LIST_CONTENT)

        webClient.uploadFiles(listOf(fileListFile, fileList))
        webClient.validateFileList(fileList.name)

        webClient.deleteFile(fileListFile.name)
        webClient.deleteFile(fileList.name)
    }

    companion object {
        private val FILE_LIST_CONTENT = jsonArray(
            jsonObj {
                "path" to "Plate1.tif"
                "size" to 290
                "type" to "file"
            }
        ).toString()
    }

    object RegUser : TestUser {
        override val username = "User File List Validation"
        override val email = "biostudies-mgmt-filelist-v@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
    }
}
