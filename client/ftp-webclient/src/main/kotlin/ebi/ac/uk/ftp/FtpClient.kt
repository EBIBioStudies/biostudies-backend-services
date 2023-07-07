package ebi.ac.uk.ftp

import mu.KotlinLogging
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPSClient
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

fun interface InputStreamSource {
    fun inputStream(): InputStream
}

class FtpClient(
    private val ftpUser: String,
    private val ftpPassword: String,
    private val ftpUrl: String,
    private val ftpPort: Int,
) {
    fun uploadFile(path: Path, source: InputStreamSource) {
        execute { ftp -> source.inputStream().use { ftp.storeFile(path.toString(), it) } }
    }

    fun downloadFile(path: Path, source: OutputStream) {
        execute { ftp -> ftp.retrieveFile(path.toString(), source) }
    }

    fun createFolder(path: Path) {
        val paths = path.runningReduce { acc, value -> acc.resolve(value) }
        execute { ftp -> paths.forEach { ftp.makeDirectory(it.toString()) } }
    }

    fun listFiles(path: Path): List<org.apache.commons.net.ftp.FTPFile> {
        return execute { ftp ->
            ftp.changeWorkingDirectory(path.toString())
            ftp.listFiles().toList()
        }
    }

    fun deleteFile(path: Path) {
        execute { ftp ->
            val fileDeleted = ftp.deleteFile(path.toString())
            if (fileDeleted.not()) ftp.removeDirectory(path.toString())
        }
    }

    /**
     * Executes operations creating a new Ftp Client class every time as
     * @see [documented](https://cwiki.apache.org/confluence/display/COMMONS/Net+FrequentlyAskedQuestions)
     * class is not thread safe.
     */
    private fun <T> execute(function: (FTPClient) -> T): T {
        val ftp = FTPSClient()
        logger.info { "connecting to $ftpUrl, $ftpPort" }
        ftp.connect(ftpUrl, ftpPort)
        ftp.login(ftpUser, ftpPassword)
        ftp.enterLocalPassiveMode()
        val result = function(ftp)
        ftp.logout()
        ftp.disconnect()
        return result
    }
}
