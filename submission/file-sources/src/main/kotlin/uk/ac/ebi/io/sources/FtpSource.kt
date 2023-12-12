package uk.ac.ebi.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields
import org.apache.commons.net.ftp.FTPFile
import uk.ac.ebi.io.builder.createFile
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createTempFile

/**
 *  Ftp source. Mix both ftp protocol to validate file presence and direct ftp mount point to access file content.
 *  This seperation is necesary as files are check in backend instance with no access to FTP file system while
 *  processing is executed in data mover which can access moint point.
 */
class FtpSource(
    override val description: String,
    private val ftpUrl: Path,
    private val nfsPath: Path,
    private val ftpClient: FtpClient,
) : FilesSource {
    override suspend fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        val filePath = if (type == FileFields.DIRECTORY_TYPE.value) path.removeSuffix(".zip") else path
        return findFile(filePath)?.let {
            createFile(
                path = filePath,
                file = nfsPath.resolve(filePath).toFile(),
                size = it.size,
                attributes = attributes
            )
        }
    }

    private fun findFile(filePath: String): FTPFile? {
        val ftpPath = ftpUrl.resolve(filePath)
        val files = ftpClient.listFiles(ftpPath)
        if (files.isEmpty()) return null
        return files.first()
    }

    override suspend fun getFileList(path: String): File? {
        val ftpPath = ftpUrl.resolve(path)
        val exists = ftpClient.listFiles(ftpPath).isNotEmpty()
        return when (exists) {
            true -> downloadFile(ftpUrl.resolve(path))
            false -> null
        }
    }

    private fun downloadFile(path: Path): File {
        val tempFile = createTempFile().toFile()
        ftpClient.downloadFile(path, tempFile.outputStream())
        return tempFile
    }
}
