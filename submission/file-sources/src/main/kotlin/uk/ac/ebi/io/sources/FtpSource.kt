package uk.ac.ebi.io.sources

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.constants.FileFields
import org.apache.commons.net.ftp.FTPFile
import uk.ac.ebi.io.builder.createFile
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createTempFile

/**
 *  Ftp source. Mix both ftp protocol to validate file presence and direct ftp mount point to access file content.
 *  This separation is necessary as files are check in backend instance with no access to FTP file system while
 *  processing is executed in data mover which can access mount point.
 */
class FtpSource(
    override val description: String,
    private val ftpUrl: Path,
    private val nfsPath: Path,
    private val ftpClient: FtpClient,
) : FilesSource {
    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile? {
        val filePath = if (type == FileFields.DIRECTORY_TYPE.value) path.removeSuffix(".zip") else path
        return findFile(filePath)?.let {
            createFile(
                path = filePath,
                file = nfsPath.resolve(filePath).toFile(),
                size = it.size,
                type = if (it.isDirectory) DIR else FILE,
                attributes = attributes,
            )
        }
    }

    override suspend fun getFileList(path: String): File? = findFile(path)?.let { downloadFile(ftpUrl.resolve(path)) }

    private suspend fun findFile(filePath: String): FTPFile? {
        val ftpPath = ftpUrl.resolve(filePath)
        return ftpClient.findFile(ftpPath)
    }

    private suspend fun downloadFile(path: Path): File {
        val tempFile = createTempFile(suffix = path.fileName.toString()).toFile()
        tempFile.outputStream().use { ftpClient.downloadFile(path, it) }
        return tempFile
    }
}
