package ac.uk.ebi.biostd.itest.test.filelist

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileListValidationTest(
    @Autowired private val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(RegUser)
            webClient = getWebClient(serverPort, RegUser)
        }

    @BeforeEach
    fun beforeEach() {
        FileUtils.cleanDirectory(tempFolder)
    }

    @Nested
    inner class InvalidCases {
        @Test
        fun `11-1 Filelist validation when blank file list`() {
            val fileList = tempFolder.createFile("BlankFileList.json")

            webClient.uploadFile(fileList)

            val exception = assertThrows(WebClientException::class.java) { webClient.validateFileList(fileList.name) }
            assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            assertThat(exception).hasMessageContaining("Expected content to be an array")

            webClient.deleteFile(fileList.name)
        }

        @Test
        fun `11-2 Filelist validation when empty file list`() {
            val fileList = tempFolder.createFile("EmptyFileList.tsv", "Files\tAttr1")

            webClient.uploadFile(fileList)

            val exception = assertThrows(WebClientException::class.java) { webClient.validateFileList(fileList.name) }
            assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(exception).hasMessageContaining("A file list should contain at least one file")

            webClient.deleteFile(fileList.name)
        }

        @Test
        fun `11-3 Filelist validation when unsupported file list format`() {
            val fileList = tempFolder.createFile("image.jpg")

            webClient.uploadFile(fileList)

            val exception =
                assertThrows(WebClientException::class.java) {
                    webClient.validateFileList(fileList.name)
                }

            assertThat(exception.statusCode).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            assertThat(exception).hasMessageContaining("Unsupported page tab format image.jpg")

            webClient.deleteFile(fileList.name)
        }

        @Test
        fun `11-4 Filelist when missing files`() {
            val fileList = tempFolder.createFile("InvalidNfsFileList.json", FILE_LIST_CONTENT)

            webClient.uploadFile(fileList)

            val exception = assertThrows(WebClientException::class.java) { webClient.validateFileList(fileList.name) }
            assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            val expectedError =
                jsonObj {
                    "log" to
                        jsonObj {
                            "level" to "ERROR"
                            "message" to
                                """
                                The following files could not be found:
                                  - Plate1.tif
                                List of available sources:
                                  - Provided Db files
                                  - biostudies-mgmt-filelist-v@ebi.ac.uk user files
                                """.trimIndent()
                            "subnodes" to jsonArray()
                        }
                    "status" to "FAIL"
                }

            assertEquals(expectedError.toString(), exception.message, JSONCompareMode.LENIENT)
            webClient.deleteFile(fileList.name)
        }
    }

    @Nested
    inner class ValidCases {
        @Test
        fun `11-5 Filelist validation when valid filelist`() =
            runTest {
                val previousVersion =
                    tsv {
                        line("Submission", "S-FLV123")
                        line()

                        line("Study")
                        line()

                        line("File", "File2.tif")
                        line()
                    }.toString()

                val file1 = tempFolder.createFile("File1.tif", "content-1")
                val file2 = tempFolder.createFile("File2.tif", "content-2")
                val fileListContent =
                    tsv {
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
            }

        @Test
        fun `11-6 Filelist validation when valid filelist with root path`() {
            val file = tempFolder.createFile("File1.tif", "content-1")
            val fileListContent =
                tsv {
                    line("Files")
                    line("File1.tif")
                    line()
                }.toString()

            val fileList = tempFolder.createFile("RootPathFileList.tsv", fileListContent)

            webClient.uploadFiles(listOf(file, fileList), "root-path")

            webClient.validateFileList(fileList.name, rootPath = "root-path")

            webClient.deleteFile(file.name)
            webClient.deleteFile(fileList.name)
        }

        @Test
        fun `11-7 Filelist validation when valid filelist on behalf another user`() =
            runTest {
                securityTestService.ensureUserRegistration(RegularUser)

                val fileListFile = tempFolder.createFile("Plate1.tif")
                val fileList = tempFolder.createFile("ValidOnBehalfFileList.json", FILE_LIST_CONTENT)

                webClient.uploadFiles(listOf(fileListFile, fileList))

                val onBehalfClient =
                    SecurityWebClient
                        .create("http://localhost:$serverPort")
                        .getAuthenticatedClient(RegUser.email, RegUser.password, RegularUser.email)

                onBehalfClient.validateFileList(fileList.name)

                webClient.deleteFile(fileListFile.name)
                webClient.deleteFile(fileList.name)
            }
    }

    companion object {
        private val FILE_LIST_CONTENT =
            jsonArray(
                jsonObj {
                    "path" to "Plate1.tif"
                    "size" to 290
                    "type" to "file"
                },
            ).toString()
    }

    object RegUser : TestUser {
        override val username = "User File List Validation"
        override val email = "biostudies-mgmt-filelist-v@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
    }
}
