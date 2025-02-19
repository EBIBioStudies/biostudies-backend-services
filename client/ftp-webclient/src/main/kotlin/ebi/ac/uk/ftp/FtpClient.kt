package ebi.ac.uk.ftp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

interface FtpClient {
    /**
     * Upload the given input stream in the provided FTP location. Stream is closed after transfer completion.
     */
    suspend fun uploadFiles(
        folder: Path,
        files: List<Pair<Path, () -> InputStream>>,
    )

    /**
     * Upload the given input stream in the provided FTP location. Stream is closed after transfer completion.
     */
    suspend fun uploadFile(
        path: Path,
        source: () -> InputStream,
    )

    /**
     * Download the given file in the output stream. Output stream is NOT closed after completion.
     */
    suspend fun downloadFile(
        path: Path,
        source: OutputStream,
    )

    /**
     * Create the given folder. As FTP does not support nested folder creation in a single path the full path is
     * transverse and required missing folder are created.
     */
    suspend fun createFolder(path: Path)

    /**
     * List the files in the given path.
     */
    suspend fun listFiles(path: Path): List<FTPFile>

    /**
     * Delete the file or folder in the given path.
     */
    suspend fun deleteFile(path: Path)

    suspend fun getFile(path: Path): FTPFile?

    companion object {
        @Suppress("LongParameterList")
        fun create(
            ftpUser: String,
            ftpPassword: String,
            ftpUrl: String,
            ftpPort: Int,
            ftpRootPath: String,
            defaultTimeout: Long,
            connectionTimeout: Long,
        ): FtpClient =
            SimpleFtpClient(
                ftpUser,
                ftpPassword,
                ftpUrl,
                ftpPort,
                ftpRootPath,
                defaultTimeout,
                connectionTimeout,
            )
    }
}

@Suppress("LongParameterList", "TooManyFunctions")
private class SimpleFtpClient(
    private val ftpUser: String,
    private val ftpPassword: String,
    private val ftpUrl: String,
    private val ftpPort: Int,
    private val ftpRootPath: String,
    private val defaultTimeout: Long,
    private val connectionTimeout: Long,
) : FtpClient {
    override suspend fun uploadFiles(
        folder: Path,
        files: List<Pair<Path, () -> InputStream>>,
    ) {
        withContext(Dispatchers.IO) {
            ftpClient().execute { ftp ->
                for ((path, inputStream) in files) {
                    ftp.createFtpFolder(path.parent)
                    ftp.setFileType(FTP.BINARY_FILE_TYPE)
                    inputStream().use { ftp.storeFile(path.toString(), it) }
                }
            }
        }
    }

    override suspend fun uploadFile(
        path: Path,
        source: () -> InputStream,
    ) {
        withContext(Dispatchers.IO) {
            ftpClient().execute { ftp ->
                ftp.createFtpFolder(path.parent)
                source().use { ftp.storeFile(path.toString(), it) }
            }
        }
    }

    override suspend fun downloadFile(
        path: Path,
        source: OutputStream,
    ) {
        withContext(Dispatchers.IO) {
            ftpClient().execute { ftp -> ftp.retrieveFile(path.toString(), source) }
        }
    }

    override suspend fun createFolder(path: Path) {
        withContext(Dispatchers.IO) {
            ftpClient().execute { ftp -> ftp.createFtpFolder(path) }
        }
    }

    override suspend fun listFiles(path: Path): List<FTPFile> =
        withContext(Dispatchers.IO) {
            ftpClient().executeRestoringWorkingDirectory { ftp ->
                val changed = ftp.changeWorkingDirectory(path.toString())
                if (changed) ftp.listAllFiles() else emptyList()
            }
        }

    override suspend fun getFile(path: Path): FTPFile? =
        withContext(Dispatchers.IO) {
            ftpClient().executeRestoringWorkingDirectory { ftp ->
                val changed = ftp.changeWorkingDirectory(path.parent.toString())
                if (changed) ftp.listFiles(path.fileName.toString()).firstOrNull() else null
            }
        }

    override suspend fun deleteFile(path: Path) {
        withContext(Dispatchers.IO) {
            ftpClient().executeRestoringWorkingDirectory { ftp ->
                suspend fun deleteDirectory(dirPath: Path) {
                    ftp.changeWorkingDirectory(dirPath.toString())
                    ftp.listAllFiles().forEach { deleteFile(Paths.get(dirPath.toString(), it.name)) }

                    ftp.changeToParentDirectory()
                    ftp.removeDirectory(dirPath.fileName.toString())
                }

                val fileDeleted = ftp.deleteFile(path.toString())
                if (fileDeleted.not()) deleteDirectory(path)
            }
        }
    }

    private fun ftpClient(): FTPClient {
        val ftp = ftpClient(connectionTimeout.milliseconds, defaultTimeout.milliseconds)
        logger.debug { "Connecting to $ftpUrl, $ftpPort" }
        ftp.connect(ftpUrl, ftpPort)
        ftp.login(ftpUser, ftpPassword)
        ftp.changeWorkingDirectory(ftpRootPath)
        ftp.setFileType(FTP.BINARY_FILE_TYPE)
        ftp.listHiddenFiles = true
        ftp.enterLocalPassiveMode()
        return ftp
    }

    private fun FTPClient.closeConnection() {
        logout()
        disconnect()
        logger.debug { "Disconnected from $ftpUrl, $ftpPort" }
    }

    /**
     * As FTP does not support nested folder creation in a single path the full path is
     * transverse and required missing folder are created.
     */
    private suspend fun FTPClient.createFtpFolder(path: Path?) =
        withContext(Dispatchers.IO) {
            val paths = path?.runningReduce { acc, value -> acc.resolve(value) }
            paths?.forEach { makeDirectory(it.toString()) }
        }

    private suspend fun FTPClient.listAllFiles(): List<FTPFile> =
        withContext(Dispatchers.IO) {
            listFiles().filterNot { it.name == "." || it.name == ".." }.toList()
        }

    private suspend fun <T> FTPClient.execute(action: suspend (FTPClient) -> T): T =
        try {
            action(this)
        } finally {
            closeConnection()
        }

    /**
     * As Ftp clients are re-used we need to guarantee that, if the working directory is changed, it is restored after
     * the operation is completed.
     */
    private suspend fun <T> FTPClient.executeRestoringWorkingDirectory(action: suspend (FTPClient) -> T): T {
        val source = printWorkingDirectory()
        return try {
            action(this)
        } finally {
            changeWorkingDirectory(source)
            closeConnection()
        }
    }
}
