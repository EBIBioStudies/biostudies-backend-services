package uk.ac.ebi.biostd.itest.test.files

import ac.uk.ebi.biostd.common.properties.StorageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.ac.ebi.biostd.api.UserFile
import uk.ac.ebi.biostd.api.UserFileType.DIR
import uk.ac.ebi.biostd.api.UserFileType.FILE
import uk.ac.ebi.biostd.client.exception.WebClientException
import uk.ac.ebi.biostd.client.integration.web.BioWebClient
import uk.ac.ebi.biostd.io.ext.createDirectory
import uk.ac.ebi.biostd.io.ext.createFile
import uk.ac.ebi.biostd.io.ext.md5
import uk.ac.ebi.biostd.itest.common.SecurityTestService
import uk.ac.ebi.biostd.itest.entities.TestUser
import uk.ac.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import uk.ac.ebi.biostd.itest.itest.getWebClient
import java.io.File
import java.nio.file.Files
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
            assertThat((error as WebClientException).message)
                .containsIgnoringCase("does not exist")
                .contains(originalName)
        }

    @ParameterizedTest(name = "17-8 renamed file should not exist under original name using {0}")
    @MethodSource("webClients")
    fun `17-8 original file not accessible after rename`(webClient: BioWebClient) =
        runTest {
            val testPath = "test-folder-17-8"
            val originalName = "a_file.txt"
            val newName = "a_new_name_file.txt"

            val file = tempFolder.createFile(originalName, "An example content")

            webClient.uploadFiles(listOf(file), relativePath = testPath)
            webClient.renameFile(testPath, file.name, newName)

            val fileNotExistingAfterRename =
                runCatching { webClient.downloadFile(originalName, testPath) }
                    .exceptionOrNull()
            assertThat(fileNotExistingAfterRename).describedAs("Downloading a renamed file should fail").isNotNull()

            webClient.deleteFile(testPath)
        }

    @ParameterizedTest(name = "17-9 rename to existing file name should fail using {0}")
    @MethodSource("webClients")
    fun `17-9 rename to existing file name`(webClient: BioWebClient) =
        runTest {
            val testPath = "test-folder-17-9"
            val existingFileName = "existing_file.txt"
            val targetFileName = "target_file.txt"

            val existingFile = tempFolder.createFile(existingFileName, "Existing content")
            val targetFile = tempFolder.createFile(targetFileName, "Target content")

            webClient.uploadFiles(listOf(existingFile), relativePath = testPath)
            webClient.uploadFiles(listOf(targetFile), relativePath = testPath)

            val renameError =
                runCatching { webClient.renameFile(testPath, existingFileName, targetFileName) }
                    .exceptionOrNull()
            assertThat(renameError).describedAs("Rename to a pre-existing file should fail").isNotNull()
            assertThat(renameError).isInstanceOf(WebClientException::class.java)
            assertThat((renameError as WebClientException).message)
                .contains(targetFileName)
                .contains("already exists")

            val finalFiles = webClient.listUserFiles(relativePath = testPath)
            assertThat(finalFiles).hasSize(2)
            assertThat(finalFiles)
                .anyMatch { it.name == existingFileName && it.type == FILE }
                .anyMatch { it.name == targetFileName && it.type == FILE }

            assertThat(webClient.downloadFile(existingFileName, testPath).md5()).isEqualTo(existingFile.md5())
            assertThat(webClient.downloadFile(targetFileName, testPath).md5()).isEqualTo(targetFile.md5())

            webClient.deleteFile(testPath)
        }

    @Test
    fun `17-10 upload cluster accessible directory as flattened files`() =
        runTest {
            val webClient = getWebClient(serverPort, FilesUser)
            val source = tempFolder.createDirectory("cluster-source-17-10")
            val innerFolder = source.resolve("inner").apply { mkdirs() }
            source.resolve("root.txt").writeText("root content")
            innerFolder.resolve("inner.txt").writeText("inner content")

            val jobId = webClient.uploadFilesThroughCluster(source, CLUSTER_TARGET)
            withContext(Dispatchers.Default) {
                withTimeout(CLUSTER_JOB_TIMEOUT_MILLIS) {
                    while (true) {
                        val status = webClient.getClusterJobStatus(jobId)
                        if (status == "COMPLETED") break
                        check(status == "RUNNING") { "Cluster upload job $jobId failed with status $status" }
                        delay(CLUSTER_POLL_MILLIS)
                    }
                }
            }

            assertThat(webClient.listUserFiles(CLUSTER_TARGET))
                .extracting<String> { it.name }
                .containsExactlyInAnyOrder("root.txt", "inner.txt")
            webClient.deleteFile(CLUSTER_TARGET)
        }

    @ParameterizedTest(name = "17-11 reject user file operations when the {0} root is missing")
    @EnumSource(StorageMode::class)
    fun `17-11 missing user root`(mode: StorageMode) =
        runTest {
            val user =
                object : TestUser {
                    override val username = "Missing Root $mode User"
                    override val email = "files-missing-root-${mode.name.lowercase()}@ebi.ac.uk"
                    override val password = "12345"
                    override val superUser = false
                    override val storageMode = mode
                }
            securityTestService.ensureUserRegistration(user)
            val webClient = getWebClient(serverPort, user)
            val root = securityTestService.getSecurityUser(user.email).userFolder.path
            assertThat(webClient.listUserFiles()).isEmpty()

            val file = tempFolder.createFile("missing-root-${mode.name}.txt", "Example content")
            val operations =
                mapOf<String, suspend () -> Unit>(
                    "query" to { webClient.listUserFiles() },
                    "download" to { webClient.downloadFile(file.name) },
                    "upload" to { webClient.uploadFile(file) },
                    "cluster upload" to { webClient.uploadFilesThroughCluster(file) },
                    "delete" to { webClient.deleteFile(file.name) },
                    "rename" to { webClient.renameFile("", file.name, "renamed.txt") },
                    "create folder" to { webClient.createFolder("new-folder") },
                )

            val backup = Files.move(root, root.resolveSibling("${root.fileName}.backup"))
            try {
                operations.forEach { (operation, request) ->
                    val error = runCatching { request() }.exceptionOrNull()
                    assertThat(error).describedAs(operation).isInstanceOf(WebClientException::class.java)
                    assertThat((error as WebClientException).statusCode).describedAs(operation).isEqualTo(INTERNAL_SERVER_ERROR)
                    assertThat(error).hasMessageContaining("The user folder was not found")
                }
                assertThat(root).doesNotExist()
            } finally {
                Files.move(backup, root)
            }
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
    }

    object FilesFtpUser : TestUser {
        override val username = "Files Ftp User"
        override val email = "files-biostudiesftp--mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
        override val storageMode = StorageMode.NFS
    }

    private companion object {
        private const val CLUSTER_TARGET = "cluster-target-17-10"
        private const val CLUSTER_JOB_TIMEOUT_MILLIS = 30_000L
        private const val CLUSTER_POLL_MILLIS = 100L
    }
}
