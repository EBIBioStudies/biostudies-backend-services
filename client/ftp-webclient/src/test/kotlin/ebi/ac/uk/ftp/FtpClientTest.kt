package ebi.ac.uk.ftp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths

class FtpClientTest {
    private val ftpServer = createFtpServer().apply { start() }
    private val testInstance = FtpClient.create(
        FTP_USER,
        FTP_PASSWORD,
        ftpServer.getUrl(),
        ftpServer.ftpPort,
        FTP_ROOT_PATH,
    )

    @BeforeEach
    fun beforeEach() {
        testInstance.deleteFile(Paths.get(""))
    }

    @Nested
    inner class FileExists {

        val rootPath = Paths.get("")

        @Test
        fun whenNotExistInRootPath() {
            val noExits = rootPath.resolve("test-file-1")

            assertThat(testInstance.exists(noExits)).isFalse()
        }

        @Test
        fun whenExistsInRootPaath() {
            val tempFile = createTempFile("test-file-1")
            val filePath = rootPath.resolve("file1.txt")

            testInstance.uploadFile(filePath, { tempFile.inputStream() })

            assertThat(testInstance.exists(filePath)).isTrue()
        }
    }

    @Test
    fun uploadFileInFolder() {
        val tempFile = createTempFile("test-file-1")
        val rootPath = Paths.get("")

        testInstance.uploadFile(rootPath.resolve("a-folder").resolve("file1.txt")) { tempFile.inputStream() }

        val files = testInstance.listFiles(rootPath)
        assertThat(files).hasOnlyOneElementSatisfying { it.name == "a-folder" && it.isDirectory }

        val folderFiles = testInstance.listFiles(rootPath.resolve("a-folder"))
        assertThat(folderFiles).hasOnlyOneElementSatisfying { it.name == "file1.txt" && it.isFile }
    }

    @Test
    fun `delete folder`() {
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

    @Test
    fun `upload a file, list it and download it`() {
        val tempFile = createTempFile("test-file")

        val rootPath = Paths.get("")
        val filePath = rootPath.resolve("test-file.txt")

        testInstance.uploadFile(filePath) { tempFile.inputStream() }

        val files = testInstance.listFiles(rootPath)
        assertThat(files).hasSize(1)
        val file = files.first()
        assertThat(file.name).isEqualTo("test-file.txt")

        val outputFile = createTempFile()
        outputFile.outputStream().use { testInstance.downloadFile(filePath, it) }
        assertThat(outputFile).hasSameContentAs(tempFile)
    }

    companion object {
        fun createFtpServer(): FtpServer {
            return FtpServer.createServer(
                FtpConfig(
                    sslConfig = SslConfig(File(this::class.java.getResource("/mykeystore.jks")!!.toURI()), "123456"),
                    userName = FTP_USER,
                    password = FTP_PASSWORD
                )
            )
        }

        const val FTP_USER = "ftpUser"
        const val FTP_PASSWORD = "ftpPassword"
        const val FTP_ROOT_PATH = ".test"
    }
}
