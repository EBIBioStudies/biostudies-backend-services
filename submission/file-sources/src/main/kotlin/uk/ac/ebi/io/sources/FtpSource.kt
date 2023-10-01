package uk.ac.ebi.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields
import uk.ac.ebi.io.builder.createFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.outputStream

class FtpSource(
    override val description: String,
    private val basePath: Path,
    private val ftpClient: FtpClient,
) : FilesSource {
    override suspend fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        val filePath = if (type == FileFields.DIRECTORY_TYPE.value) path.removeSuffix(".zip") else path
        return findFile(filePath)?.let { createFile(filePath, it, attributes) }
    }

    private fun findFile(filePath: String): File? {
        val ftpPath = basePath.resolve(filePath)
        val files = ftpClient.listFiles(ftpPath)
        if (files.isEmpty()) return null

        val fileName = filePath.substringAfterLast("/")
        val target = Files.createTempFile("ftp-file", fileName)

        target.outputStream().use { ftpClient.downloadFile(ftpPath, it) }
        return target.toFile()
    }

    override suspend fun getFileList(path: String): File? {
        return findFile(path)
    }
}
