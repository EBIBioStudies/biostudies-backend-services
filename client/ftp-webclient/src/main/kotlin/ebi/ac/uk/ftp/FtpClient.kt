package ebi.ac.uk.ftp

import org.apache.commons.net.ftp.FTPClient
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

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
        return execute { ftp -> ftp.listFiles(path.toString()).toList() }
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
        val ftp = FTPClient()
        ftp.connect(ftpUrl, ftpPort)
        ftp.enterLocalPassiveMode()
        ftp.login(ftpUser, ftpPassword)
        val result = function(ftp)
        ftp.disconnect()
        return result
    }
}
