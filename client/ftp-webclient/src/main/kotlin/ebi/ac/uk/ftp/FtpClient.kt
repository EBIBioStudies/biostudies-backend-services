package ebi.ac.uk.ftp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths

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
        ): FtpClient {
            val connectionPool =
                FTPClientPool(
                    ftpUser,
                    ftpPassword,
                    ftpUrl,
                    ftpPort,
                    ftpRootPath,
                    defaultTimeout,
                    connectionTimeout,
                )

            return SimpleFtpClient(connectionPool)
        }
    }
}

private class SimpleFtpClient(
    private val ftpClientPool: FTPClientPool,
) : FtpClient {
    override suspend fun uploadFiles(
        folder: Path,
        files: List<Pair<Path, () -> InputStream>>,
    ): Unit =
        withContext(Dispatchers.IO) {
            ftpClientPool.execute { ftp ->
                for ((path, inputStream) in files) {
                    ftp.createFtpFolder(path.parent)
                    ftp.setFileType(FTP.BINARY_FILE_TYPE)
                    inputStream().use { ftp.storeFile(path.toString(), it) }
                }
            }
        }

    /**
     * Upload the given input stream in the provided FTP location. Stream is closed after transfer completion.
     */
    override suspend fun uploadFile(
        path: Path,
        source: () -> InputStream,
    ): Unit =
        withContext(Dispatchers.IO) {
            ftpClientPool.execute { ftp ->
                ftp.createFtpFolder(path.parent)
                source().use { ftp.storeFile(path.toString(), it) }
            }
        }

    /**
     * Download the given file in the output stream. Output stream is NOT closed after completion.
     */
    override suspend fun downloadFile(
        path: Path,
        source: OutputStream,
    ): Unit =
        withContext(Dispatchers.IO) {
            ftpClientPool.execute { ftp -> ftp.retrieveFile(path.toString(), source) }
        }

    /**
     * Create the given folder.
     */
    override suspend fun createFolder(path: Path): Unit =
        withContext(Dispatchers.IO) {
            ftpClientPool.execute { ftp -> ftp.createFtpFolder(path) }
        }

    /**
     * List the files in the given path.
     */
    override suspend fun listFiles(path: Path): List<FTPFile> =
        ftpClientPool.executeRestoringWorkingDirectory { ftp ->
            val changed = ftp.changeWorkingDirectory(path.toString())
            if (changed) ftp.listAllFiles() else emptyList()
        }

    /**
     * Delete the file or folder in the given path.
     */
    override suspend fun deleteFile(path: Path) {
        ftpClientPool.executeRestoringWorkingDirectory { ftp ->
            /**
             * As delete multiple files are not supported by apache client its necessary delete by iterating over each
             * file.
             */
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

    /**
     * As FTP does not support nested folder creation in a single path the full path is
     * transverse and required missing folder are created.
     */
    private suspend fun FTPClient.createFtpFolder(path: Path?) =
        withContext(Dispatchers.IO) {
            val paths = path?.runningReduce { acc, value -> acc.resolve(value) }
            paths?.forEach { makeDirectory(it.toString()) }
        }

    /**
     * As Ftp clients are re-used we need to guarantee that, if the working directory is changed, it is restored after
     * the operation is completed.
     */
    private suspend fun <T> FTPClientPool.executeRestoringWorkingDirectory(action: suspend (FTPClient) -> T): T =
        withContext(Dispatchers.IO) {
            execute {
                val source = it.printWorkingDirectory()
                try {
                    action(it)
                } finally {
                    it.changeWorkingDirectory(source)
                }
            }
        }

    private suspend fun FTPClient.listAllFiles(): List<FTPFile> =
        withContext(Dispatchers.IO) {
            listFiles().filterNot { it.name == "." || it.name == ".." }.toList()
        }
}
