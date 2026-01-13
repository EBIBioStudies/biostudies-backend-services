package ebi.ac.uk.ftp

import ebi.ac.uk.test.createFile
import ebi.ac.uk.test.createTempFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.RetryingTest
import java.io.File
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class)
class FtpClientTest(
    private val temporaryFolder: TemporaryFolder,
) {
    private val ftpServer = createFtpServer(temporaryFolder.createDirectory("ftp")).apply { start() }
    private val testInstance =
        FtpClient.create(
            FTP_USER,
            FTP_PASSWORD,
            ftpServer.getUrl(),
            ftpServer.ftpPort,
            FTP_TIMEOUT,
            FTP_TIMEOUT,
        )

    @BeforeEach
    fun setUp() =
        runBlocking {
            testInstance.deleteFile(HOME)
        }

    @RetryingTest(TEST_RETRY)
    fun uploadFileInFolder() =
        runTest {
            val tempFile = createTempFile("test-file-1")
            val rootPath = Paths.get("")

            testInstance.uploadFile(rootPath.resolve("a-folder").resolve("file1.txt")) { tempFile.inputStream() }

            val files = testInstance.listFiles(rootPath)
            assertThat(files).satisfiesOnlyOnce { it.name == "a-folder" && it.isDirectory } // .hasOnlyOneElementSatisfying {  }

            val folderFiles = testInstance.listFiles(rootPath.resolve("a-folder"))
            assertThat(folderFiles).satisfiesOnlyOnce { it.name == "file1.txt" && it.isFile }
        }

    @RetryingTest(TEST_RETRY)
    fun findFile() =
        runTest {
            val tempFile = temporaryFolder.createFile("test-file-1", "content")
            val fileFolder = Paths.get("").resolve("a-folder")

            val filePath = fileFolder.resolve("file1.txt")

            testInstance.uploadFile(filePath) { tempFile.inputStream() }

            val result = testInstance.findFile(filePath)
            assertThat(result).isNotNull()
            assertThat(result!!.name).isEqualTo("file1.txt")
            assertThat(testInstance.findFile(fileFolder.resolve("file2.txt"))).isNull()
        }

    @RetryingTest(TEST_RETRY)
    fun `delete folder`() =
        runTest {
            val tempFile = createTempFile("test-file-1")

            val rootPath = Paths.get("")
            val folder = Paths.get("a-folder")

            testInstance.createFolder(folder)
            testInstance.uploadFile(folder.resolve("file1.text")) { tempFile.inputStream() }

            val files = testInstance.listFiles(folder)
            assertThat(files).hasSize(1)
            val file = files.first()
            assertThat(file.name).isEqualTo("file1.text")

            val folders = testInstance.listFiles(rootPath)
            assertThat(folders).hasSize(1)
            assertThat(folders.filter { it.isDirectory }.map { it.name }).containsExactly("a-folder")

            testInstance.deleteFile(folder)
            assertThat(testInstance.listFiles(folder)).isEmpty()
        }

    @RetryingTest(TEST_RETRY)
    fun `rename a file`() =
        runTest {
            val tempFile = createTempFile("test-file-rename")

            val rootPath = Paths.get("test-folder")
            val originalPath = rootPath.resolve("original.txt")
            val newName = "renamed.txt"
            val newPath = rootPath.resolve(newName)

            testInstance.uploadFile(originalPath) { tempFile.inputStream() }

            val filesBeforeRename = testInstance.listFiles(rootPath)
            assertThat(filesBeforeRename).satisfiesOnlyOnce { it.name == "original.txt" && it.isFile }

            val result = testInstance.renameFile(originalPath, newName)
            assertThat(result).isTrue()

            assertThat(testInstance.findFile(originalPath)).isNull()
            assertThat(testInstance.findFile(newPath)).isNotNull()
        }

    @RetryingTest(TEST_RETRY)
    fun `rename a folder`() =
        runTest {
            val tempFile = createTempFile("test-file-in-folder")

            val rootPath = Paths.get("")
            val originalFolder = rootPath.resolve("original-folder")
            val newName = "renamed-folder"

            testInstance.createFolder(originalFolder)
            testInstance.uploadFile(originalFolder.resolve("file.txt")) { tempFile.inputStream() }

            val foldersBeforeRename = testInstance.listFiles(rootPath)
            assertThat(foldersBeforeRename).satisfiesOnlyOnce { it.name == "original-folder" && it.isDirectory }

            val result = testInstance.renameFile(originalFolder, newName)
            assertThat(result).isTrue()

            val foldersAfterRename = testInstance.listFiles(rootPath)
            assertThat(foldersAfterRename).satisfiesOnlyOnce { it.name == "renamed-folder" && it.isDirectory }
            assertThat(foldersAfterRename).noneMatch { it.name == "original-folder" }

            val filesInRenamedFolder = testInstance.listFiles(rootPath.resolve(newName))
            assertThat(filesInRenamedFolder).satisfiesOnlyOnce { it.name == "file.txt" && it.isFile }
        }

    @RetryingTest(TEST_RETRY)
    fun `rename non-existing file returns false`() =
        runTest {
            val rootPath = Paths.get("")
            val nonExistingPath = rootPath.resolve("non-existing.txt")

            val result = testInstance.renameFile(nonExistingPath, "new-name.txt")
            assertThat(result).isFalse()
        }

    @RetryingTest(TEST_RETRY)
    fun `rename to existing name returns false`() =
        runTest {
            val tempFile1 = createTempFile("test-file-1")
            val tempFile2 = createTempFile("test-file-2")

            val rootPath = Paths.get("")
            val file1Path = rootPath.resolve("file1.txt")
            val file2Path = rootPath.resolve("file2.txt")

            testInstance.uploadFile(file1Path) { tempFile1.inputStream() }
            testInstance.uploadFile(file2Path) { tempFile2.inputStream() }

            val result = testInstance.renameFile(file1Path, "file2.txt")
            assertThat(result).isFalse()

            val files = testInstance.listFiles(rootPath)
            assertThat(files).hasSize(2)
            assertThat(files.map { it.name }).containsExactlyInAnyOrder("file1.txt", "file2.txt")
        }

    @RetryingTest(TEST_RETRY)
    fun `upload a file, list it and download it`() =
        runTest {
            val tempFile = createTempFile("test-file")

            val rootPath = Paths.get("")
            val filePath = rootPath.resolve("test-file.txt")

            testInstance.uploadFile(filePath) { tempFile.inputStream() }

            val files = testInstance.listFiles(rootPath)
            assertThat(files).hasSize(1)
            val file = files.first()
            assertThat(file.name).isEqualTo("test-file.txt")

            val outputFile = createTempFile("")
            outputFile.outputStream().use { testInstance.downloadFile(filePath, it) }
            assertThat(outputFile).hasSameBinaryContentAs(tempFile)
        }

    companion object {
        fun createFtpServer(directory: File): FtpServer =
            FtpServer.createServer(
                FtpConfig(
                    sslConfig = SslConfig(File(this::class.java.getResource("/mykeystore.jks")!!.toURI()), "123456"),
                    userName = FTP_USER,
                    password = FTP_PASSWORD,
                    path = directory.toPath(),
                ),
            )

        private val HOME = Paths.get("")
        const val FTP_USER = "ftpUser"
        const val FTP_PASSWORD = "ftpPassword"
        const val TEST_RETRY = 3
        const val FTP_TIMEOUT = 3000L
    }
}
