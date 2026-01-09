package ac.uk.ebi.biostd.itest.test.files

import ac.uk.ebi.biostd.client.exception.WebClientException
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
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Paths
import java.util.stream.Stream

@ExtendWith(SpringExtension::class)
@TestInstance(PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserFileApiTest(
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val resourceLoader: ResourceLoader,
    @param:LocalServerPort val serverPort: Int,
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

    @ParameterizedTest(name = "17-6 rename a text file using {0}")
    @MethodSource("webClients")
    fun `17-6 user text file rename`(webClient: BioWebClient) =
        runTest {
            val testPath = "test-folder-17-6"
            val originalName = "a_file.txt"
            val newName = "a_new_name_file.txt"

            val file = tempFolder.createFile(originalName, "An example content")

            webClient.uploadFiles(listOf(file), relativePath = testPath)
            webClient.renameFile(testPath, file.name, newName)

            val files = webClient.listUserFiles(relativePath = testPath)
            assertThat(files).hasSize(1)
            assertThat(files.first().name).isEqualTo(newName)

            val resultFile = webClient.downloadFile(newName, testPath)
            assertThat(resultFile.name).isEqualTo(newName)
            assertThat(resultFile.md5()).isEqualTo(file.md5())


            // This doesn't work as downloading a file which doesn't exist is not returning an error
//            val error = runCatching { webClient.downloadFile(originalName, testPath) }.exceptionOrNull()
//            assertThat(error).describedAs("Download of original file should fail after rename").isNotNull()
//            assertThat(error).isInstanceOf(WebClientException::class.java)
//            assertThat((error as WebClientException).statusCode).isEqualTo(HttpStatus.NOT_FOUND)

            // Rename a non-existing file
            val nonExistingError = runCatching { webClient.renameFile(testPath, "non_existing.txt", "some_name.txt") }.exceptionOrNull()
            assertThat(nonExistingError).describedAs("Rename of non-existing file should fail").isNotNull()
            assertThat(nonExistingError).isInstanceOf(WebClientException::class.java)
            assertThat((nonExistingError as WebClientException).statusCode).isEqualTo(HttpStatus.NOT_FOUND)

            // Rename to a pre-existing name
            val anotherFileName = "another_file.txt"
            val anotherFile = tempFolder.createFile(anotherFileName, "Another content")
            webClient.uploadFiles(listOf(anotherFile), relativePath = testPath)

            val renameError = runCatching { webClient.renameFile(testPath, anotherFile.name, newName) }.exceptionOrNull()
            assertThat(renameError).describedAs("Rename to a pre-existing file should fail").isNotNull()
            assertThat(renameError).isInstanceOf(WebClientException::class.java)
            assertThat((renameError as WebClientException).statusCode).isEqualTo(HttpStatus.CONFLICT)

            val finalFiles = webClient.listUserFiles(relativePath = testPath)
            assertThat(finalFiles).hasSize(2)

            assertThat(finalFiles)
                .anyMatch { it.name == newName && it.type == FILE }
                .anyMatch { it.name == anotherFileName && it.type == FILE }

            assertThat(webClient.downloadFile(newName, testPath).md5()).isEqualTo(resultFile.md5())
            assertThat(webClient.downloadFile(anotherFileName, testPath).md5()).isEqualTo(anotherFile.md5())

            webClient.deleteFile(testPath)
        }

    @ParameterizedTest(name = "17-7 rename a non-existing file using {0}")
    @MethodSource("webClients")
    fun `17-7 rename non-existing file`(webClient: BioWebClient) =
        runTest {
            val testPath = "test-folder-17-7"
            val originalName = "non_existing.txt"
            val newName = "a_new_name_file.txt"

            val error = runCatching { webClient.renameFile(testPath, originalName, newName) }.exceptionOrNull()
            assertThat(error).isInstanceOf(WebClientException::class.java)
            assertThat((error as WebClientException).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
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
