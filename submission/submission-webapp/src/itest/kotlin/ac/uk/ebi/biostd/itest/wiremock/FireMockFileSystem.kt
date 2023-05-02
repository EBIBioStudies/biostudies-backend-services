package ac.uk.ebi.biostd.itest.wiremock

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FireMockFileSystem(
    private val dbFolder: Path,
    private val ftpFolder: Path,
    private val submissionFolder: Path,
    private val fireS3Service: FireS3Service,
) {
    fun saveFile(data: ByteArray, fireOid: String): File {
        val file = dbFolder.resolve(fireOid).toFile()
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeBytes(data)
        return file
    }

    fun findFileByFireId(fireOid: String): Path = dbFolder.resolve(fireOid)

    fun setPath(fireOid: String, absolutePath: String) {
        val fireFile = dbFolder.resolve(fireOid)

        val relativePath = absolutePath.removePrefix("/")
        Files.copy(fireFile, getOrCreateSubFolder(relativePath))
        fireS3Service.upload(fireFile.toFile(), absolutePath)
    }

    fun delete(absolutePath: String) {
        val relativePath = absolutePath.removePrefix("/")
        Files.deleteIfExists(ftpFolder.resolve(relativePath))
        Files.delete(submissionFolder.resolve(relativePath))
        fireS3Service.deleteFile(relativePath)
    }

    fun publish(absolutePath: String) {
        val relativePath = absolutePath.removePrefix("/")
        val source = submissionFolder.resolve(relativePath)
        val target = getOrCreateFtpFolder(relativePath)
        Files.copy(source, target)
    }

    fun unpublish(absolutePath: String) {
        val relativePath = absolutePath.removePrefix("/")
        Files.delete(ftpFolder.resolve(relativePath))
    }

    private fun getOrCreateSubFolder(relPath: String): Path {
        val file = submissionFolder.resolve(relPath)
        file.parent.toFile().mkdirs()
        return file
    }

    private fun getOrCreateFtpFolder(path: String): Path {
        val file = ftpFolder.resolve(path)
        file.parent.toFile().mkdirs()
        return file
    }
}
