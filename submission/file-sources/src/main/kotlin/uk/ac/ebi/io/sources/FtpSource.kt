package uk.ac.ebi.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields
import uk.ac.ebi.io.builder.createFile
import java.io.File
import java.nio.file.Path

class FtpSource(
    override val description: String,
    private val ftpPath: Path,
    private val nfsPath: Path,
    private val ftpClient: FtpClient,
) : FilesSource {
    override suspend fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        val filePath = if (type == FileFields.DIRECTORY_TYPE.value) path.removeSuffix(".zip") else path
        return findFile(filePath)?.let { createFile(filePath, it, attributes) }
    }

    private fun findFile(filePath: String): File? {
        val ftpPath = ftpPath.resolve(filePath)
        val files = ftpClient.listFiles(ftpPath)
        if (files.isEmpty()) return null
        return nfsPath.resolve(filePath).toFile()
    }

    override suspend fun getFileList(path: String): File? {
        return findFile(path)
    }
}
