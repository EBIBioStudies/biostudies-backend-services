package ac.uk.ebi.biostd.itest.test.files

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType.DIR
import ebi.ac.uk.api.UserFileType.FILE
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.md5
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Paths
import java.util.stream.Stream

@ExtendWith(SpringExtension::class)
@TestInstance(PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserFileApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val resourceLoader: ResourceLoader,
    @LocalServerPort val serverPort: Int,
) {
    @BeforeAll
    fun init() =
        runTest {
            securityTestService.ensureUserRegistration(FilesUser)
            securityTestService.ensureUserRegistration(FilesFtpUser)
        }

    @ParameterizedTest(name = "17-1 upload download delete file and retrieve in {0} root folder")
    @MethodSource("webClients")
    fun `17-1 root folder`(webClient: BioWebClient) =
        runTest {
            val testPath = ""
            val file = tempFolder.createFile("FileList1.txt", "An example content")
            webClient.uploadFiles(listOf(file), relativePath = testPath)

            val files = webClient.listUserFiles(relativePath = testPath)
            assertThat(files).hasSize(1)
            assertFile(files.first(), webClient.downloadFile(file.name, testPath), file, testPath)

            webClient.deleteFile("FileList1.txt", "")
            assertThat(webClient.listUserFiles(relativePath = testPath)).isEmpty()
        }

    @ParameterizedTest(name = "17-2 upload download delete file and retrieve in {0} folder")
    @MethodSource("webClients")
    fun `17-2 user folder`(webClient: BioWebClient) =
        runTest {
            val testPath = "test-folder-17-2"
            val file = tempFolder.createFile("FileList1.txt", "An example content")
            webClient.uploadFiles(listOf(file), relativePath = testPath)

            val files = webClient.listUserFiles(relativePath = testPath)
            assertThat(files).hasSize(1)
            assertFile(files.first(), webClient.downloadFile(file.name, testPath), file, testPath)

            webClient.deleteFile("FileList1.txt", testPath)
            assertThat(webClient.listUserFiles(relativePath = testPath)).isEmpty()
            webClient.deleteFile(testPath)
        }

    @ParameterizedTest(name = "17-3 upload download delete file and retrieve in {0} folder with space")
    @MethodSource("webClients")
    fun `17-3 folder with space`(webClient: BioWebClient) =
        runTest {
            val folder = "test-folder 17-3"
            val innerFolder = "$folder/test-inner-folder"
            val file = tempFolder.createFile("FileList1.txt", "An example content")
            val hiddenFile = tempFolder.createFile(".hiddenFile.txt", "Hidden file")
            val innerFile1 = tempFolder.createFile("InnerFile1.txt", "An inner file")
            val innerFile2 = tempFolder.createFile("InnerFile2.txt", "Another inner file")

            webClient.uploadFile(file, folder)
            webClient.uploadFile(hiddenFile, folder)
            webClient.uploadFile(innerFile1, innerFolder)
            webClient.uploadFile(innerFile2, innerFolder)

            assertThat(webClient.listUserFiles(folder))
                .hasSize(3)
                .anyMatch { it.name == file.name && it.type == FILE }
                .anyMatch { it.name == hiddenFile.name && it.type == FILE }
                .anyMatch { it.name == "test-inner-folder" && it.type == DIR }

            webClient.deleteFile(innerFile2.name, innerFolder)
            assertThat(webClient.listUserFiles(innerFolder))
                .hasSize(1)
                .allMatch { it.name == innerFile1.name }

            webClient.deleteFile(folder)
            assertThat(webClient.listUserFiles()).isEmpty()
        }

    @ParameterizedTest(name = "17-4 download a binary file using {0}")
    @MethodSource("webClients")
    fun `17-4 user binary file download`(webClient: BioWebClient) =
        runTest {
            val testPath = "test-folder-17-4"
            val file = resourceLoader.getResource("classpath:17.5/over200000_rows.xlsx").file

            webClient.uploadFiles(listOf(file), relativePath = testPath)

            val files = webClient.listUserFiles(relativePath = testPath)
            assertThat(files).hasSize(1)
            val resultFile = webClient.downloadFile(file.name, testPath)
            assertThat(resultFile.name).isEqualTo(file.name)
            assertThat(resultFile.md5()).isEqualTo(file.md5())
            webClient.deleteFile(testPath)
        }

    @ParameterizedTest(name = "17-5 download a text file using {0}")
    @MethodSource("webClients")
    fun `17-5 user text file download`(webClient: BioWebClient) =
        runTest {
            val testPath = "test-folder-17-5"
            val file = tempFolder.createFile("a_file.txt", "An example content")

            webClient.uploadFiles(listOf(file), relativePath = testPath)

            val files = webClient.listUserFiles(relativePath = testPath)
            assertThat(files).hasSize(1)
            val resultFile = webClient.downloadFile(file.name, testPath)
            assertThat(resultFile.name).isEqualTo(file.name)
            assertThat(resultFile.md5()).isEqualTo(file.md5())
            webClient.deleteFile(testPath)
        }

    private fun assertFile(
        resultFile: UserFile,
        downloadFile: File,
        file: File,
        relativePath: String,
    ) {
        assertThat(resultFile.name).isEqualTo(file.name)
        assertThat(resultFile.type).isEqualTo(FILE)
        assertThat(resultFile.size).isEqualTo(file.length())
        assertThat(resultFile.path).isEqualTo(Paths.get("user").resolve(relativePath).toString())
        assertThat(file).hasContent(downloadFile.readText())
    }

    private fun webClients(): Stream<Arguments> {
        val fileArg = Arguments.of(Named.of("Nfs storage user", getWebClient(serverPort, FilesUser)))
        val ftpArg = Arguments.of(Named.of("Ftp storage user", getWebClient(serverPort, FilesFtpUser)))
        return Stream.of(fileArg, ftpArg)
    }

    object FilesUser : TestUser {
        override val username = "Files User"
        override val email = "files-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
        override val storageMode = StorageMode.NFS

        override fun asRegisterRequest() = RegisterRequest(username, email, password, storageMode = "NFS")
    }

    object FilesFtpUser : TestUser {
        override val username = "Files Ftp User"
        override val email = "files-biostudiesftp--mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
        override val storageMode = StorageMode.NFS

        override fun asRegisterRequest() = RegisterRequest(username, email, password, storageMode = "FTP")
    }
}
