package ebi.ac.uk.ftp

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

interface FtpClient {
    /**
     * Upload the given input stream in the provided FTP location. Stream is closed after transfer completion.
     */
    fun uploadFiles(
        folder: Path,
        files: List<Pair<Path, () -> InputStream>>,
    )

    /**
     * Upload the given input stream in the provided FTP location. Stream is closed after transfer completion.
     */
    fun uploadFile(
        path: Path,
        source: () -> InputStream,
    )

    /**
     * Download the given file in the output stream. Output stream is NOT closed after completion.
     */
    fun downloadFile(
        path: Path,
        source: OutputStream,
    )

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

    companion object {
        fun create(
            ftpUser: String,
            ftpPassword: String,
            ftpUrl: String,
            ftpPort: Int,
            ftpRootPath: String,
        ): FtpClient {
            val connectionPool = FTPClientPool(ftpUser, ftpPassword, ftpUrl, ftpPort, ftpRootPath)
            return SimpleFtpClient(connectionPool)
        }
    }
}

private class SimpleFtpClient(
    private val ftpClientPool: FTPClientPool,
) : FtpClient {
    override fun uploadFiles(
        folder: Path,
        files: List<Pair<Path, () -> InputStream>>,
    ) {
        ftpClientPool.execute { ftp ->
            for ((path, inputStream) in files) {
                ftp.createFtpFolder(path.parent)
                inputStream().use { ftp.storeFile(path.toString(), it) }
            }
        }
    }

    /**
     * Upload the given input stream in the provided FTP location. Stream is closed after transfer completion.
     */
    override fun uploadFile(
        path: Path,
        source: () -> InputStream,
    ) {
        ftpClientPool.execute { ftp ->
            ftp.createFtpFolder(path.parent)
            source().use { ftp.storeFile(path.toString(), it) }
        }
    }

    /**
     * Download the given file in the output stream. Output stream is NOT closed after completion.
     */
    override fun downloadFile(
        path: Path,
        source: OutputStream,
    ) {
        ftpClientPool.execute { ftp -> ftp.retrieveFile(path.toString(), source) }
    }

    /**
     * Create the given folder.
     */
    override fun createFolder(path: Path) {
        ftpClientPool.execute { ftp -> ftp.createFtpFolder(path) }
    }

    /**
     * List the files in the given path.
     */
    override fun listFiles(path: Path): List<FTPFile> {
        return ftpClientPool.executeRestoringWorkingDirectory { ftp ->
            ftp.changeWorkingDirectory(path.toString())
            ftp.listFiles().toList()
        }
    }

    /**
     * Delete the file or folder in the given path.
     */
    override fun deleteFile(path: Path) {
        ftpClientPool.executeRestoringWorkingDirectory { ftp ->
            val fileDeleted = ftp.deleteFile(path.toString())
            if (fileDeleted.not()) ftp.deleteDirectory(path)
        }
    }

    /**
     * As FTP does not support nested folder creation in a single path the full path is
     * transverse and required missing folder are created.
     */
    private fun FTPClient.createFtpFolder(path: Path?) {
        val paths = path?.runningReduce { acc, value -> acc.resolve(value) }
        paths?.forEach { makeDirectory(it.toString()) }
    }

    /**
     * As Ftp clients are re-used we need to guarantee that, if the working directory is changed, it is restored after
     * the operation is completed.
     */
    private fun <T> FTPClientPool.executeRestoringWorkingDirectory(action: (FTPClient) -> T): T {
        return execute {
            val source = it.printWorkingDirectory()
            try {
                action(it)
            } finally {
                it.changeWorkingDirectory(source)
            }
        }
    }

    /**
     * As delete multiple files are not supported by apache client its neccessary delete by iterating over each file.
     */
    private fun FTPClient.deleteDirectory(dirPath: Path) {
        changeWorkingDirectory(dirPath.toString())
        listNames().forEach { deleteFile(it) }

        changeToParentDirectory()
        removeDirectory(dirPath.fileName.toString())
    }
}
