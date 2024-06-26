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
    fun saveFile(
        data: ByteArray,
        fireOid: String,
    ): File {
        val file = dbFolder.resolve(fireOid).toFile()
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeBytes(data)
        return file
    }

    fun findFileByFireId(fireOid: String): Path = dbFolder.resolve(fireOid)

    fun setPath(
        fireOid: String,
        path: String,
    ) {
        val fireFile = dbFolder.resolve(fireOid)
        Files.copy(fireFile, getOrCreateSubFolder(path))
        fireS3Service.upload(fireFile.toFile(), path)
    }

    fun delete(path: String) {
        Files.deleteIfExists(ftpFolder.resolve(path))
        Files.delete(submissionFolder.resolve(path))
        fireS3Service.deleteFile(path)
    }

    fun publish(path: String) {
        val source = submissionFolder.resolve(path)
        val target = getOrCreateFtpFolder(path)
        Files.copy(source, target)
    }

    fun unpublish(path: String) {
        Files.delete(ftpFolder.resolve(path))
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
