package ebi.ac.uk.ftp

import ebi.ac.uk.coroutines.SuspendRetryTemplate
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

class RetryFtpClient(
    private val retryTemplate: SuspendRetryTemplate,
    private val ftpClient: FtpClient,
) : FtpClient {
    override suspend fun uploadFiles(
        folder: Path,
        files: List<Pair<Path, () -> InputStream>>,
    ) = retryTemplate.execute("uploadFiles '$folder'") { ftpClient.uploadFiles(folder, files) }

    override suspend fun uploadFile(
        path: Path,
        source: () -> InputStream,
    ) = retryTemplate.execute("uploadFile '$path'") { ftpClient.uploadFile(path, source) }

    override suspend fun downloadFile(
        path: Path,
        source: OutputStream,
    ) = retryTemplate.execute("downloadFile '$path'") { ftpClient.downloadFile(path, source) }

    override suspend fun createFolder(path: Path) = retryTemplate.execute("createFolder $'$path'") { ftpClient.createFolder(path) }

    override suspend fun listFiles(path: Path): List<FTPFile> = retryTemplate.execute("listFiles '$path'") { ftpClient.listFiles(path) }

    override suspend fun deleteFile(path: Path) = retryTemplate.execute("deleteFile '$path'") { ftpClient.deleteFile(path) }

    override suspend fun renameFile(
        originalPath: Path,
        newName: String,
    ) = retryTemplate.execute("renameFile '$originalPath' -> $newName") {
        ftpClient.renameFile(originalPath, newName)
    }

    override suspend fun findFile(path: Path): FTPFile? = retryTemplate.execute("getFile '$path'") { ftpClient.findFile(path) }
}
