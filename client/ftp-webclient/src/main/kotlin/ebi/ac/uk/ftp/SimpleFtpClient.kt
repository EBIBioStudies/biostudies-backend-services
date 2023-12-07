package ebi.ac.uk.ftp

import mu.KotlinLogging
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

interface FtpClient {
    /**
     * Upload the given input stream in the provided FTP location. Stream is closed after transfer completion.
     */
    fun uploadFiles(files: List<Pair<Path, () -> InputStream>>)

    /**
     * Upload the given input stream in the provided FTP location. Stream is closed after transfer completion.
     */
    fun uploadFile(path: Path, source: () -> InputStream)

    /**
     * Download the given file in the output stream. Output stream is NOT closed after completion.
     */
    fun downloadFile(path: Path, source: OutputStream)

    /**
     * Create the given folder. As FTP does not support nested folder creation in a single path the full path is
     * transverse and required missing folder are created.
     */
    fun createFolder(path: Path)

    /**
     * List the files in the given path.
     */
    fun listFiles(path: Path): List<FTPFile>

    /**
     * Delete the file or folder in the given path.
     */
    fun deleteFile(path: Path)

    /**
     * Executes operations creating a new Ftp Client class every time as
     * @see [documented](https://cwiki.apache.org/confluence/display/COMMONS/Net+FrequentlyAskedQuestions)
     * class is not thread safe.
     */
    fun <T> execute(function: (FTPClient) -> T): T

    companion object {
        fun create(ftpUser: String, ftpPassword: String, ftpUrl: String, ftpPort: Int): FtpClient =
            SimpleFtpClient(ftpUser, ftpPassword, ftpUrl, ftpPort)
    }
}

private class SimpleFtpClient(
    private val ftpUser: String,
    private val ftpPassword: String,
    private val ftpUrl: String,
    private val ftpPort: Int,
) : FtpClient {
    override fun uploadFiles(files: List<Pair<Path, () -> InputStream>>) {
        execute { ftp ->
            for ((path, inputStream) in files) {
                inputStream().use { ftp.storeFile(path.toString(), it) }
            }
        }
    }

    /**
     * Upload the given input stream in the provided FTP location. Stream is closed after transfer completion.
     */
    override fun uploadFile(path: Path, source: () -> InputStream) {
        execute { ftp -> source().use { ftp.storeFile(path.toString(), it) } }
    }

    /**
     * Download the given file in the output stream. Output stream is NOT closed after completion.
     */
    override fun downloadFile(path: Path, source: OutputStream) {
        execute { ftp -> ftp.retrieveFile(path.toString(), source) }
    }

    /**
     * Create the given folder. As FTP does not support nested folder creation in a single path the full path is
     * transverse and required missing folder are created.
     */
    override fun createFolder(path: Path) {
        val paths = path.runningReduce { acc, value -> acc.resolve(value) }
        execute { ftp -> paths.forEach { ftp.makeDirectory(it.toString()) } }
    }

    /**
     * List the files in the given path.
     */
    override fun listFiles(path: Path): List<FTPFile> {
        return execute { ftp ->
            ftp.changeWorkingDirectory(path.toString())
            ftp.listFiles().toList()
        }
    }

    /**
     * Delete the file or folder in the given path.
     */
    override fun deleteFile(path: Path) {
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
    override fun <T> execute(function: (FTPClient) -> T): T {
        val ftp = ftpClient(3000.milliseconds, 3000.milliseconds)
        logger.info { "connecting to $ftpUrl, $ftpPort" }
        ftp.connect(ftpUrl, ftpPort)
        ftp.login(ftpUser, ftpPassword)
        ftp.enterLocalPassiveMode()
        val result = function(ftp)
        ftp.logout()
        ftp.disconnect()
        logger.info { "disconnecting to $ftpUrl, $ftpPort" }
        return result
    }
}
