package ebi.ac.uk.ftp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.createTempFile
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

class FtpClientTest {
    private val ftpServer = createFtpServer().apply { start() }
    private val testInstance = FtpClient.create(FTP_USER, FTP_PASSWORD, ftpServer.getUrl(), ftpServer.ftpPort)

    @Test
    fun `upload a file, list and download it`() {
        val tempFile = createTempFile()
        tempFile.writeText("test-file")

        val rootPath = Paths.get("");
        val filePath = rootPath.resolve("test-file.txt")

        testInstance.uploadFile(filePath, { tempFile.inputStream() })

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
                    sslConfig = SslConfig(File(this::class.java.getResource("/mykeystore.jks").toURI()), "123456"),
                    userName = FTP_USER,
                    password = FTP_PASSWORD
                )
            )
        }

        const val FTP_USER = "ftpUser"
        const val FTP_PASSWORD = "ftpPassword"
    }
}
