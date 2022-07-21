package ac.uk.ebi.biostd.itest.wiremock

import ac.uk.ebi.biostd.itest.wiremock.handlers.FireException
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.regex.match
import ebi.ac.uk.util.regex.secondGroup
import org.springframework.http.HttpStatus.CONFLICT
import uk.ac.ebi.fire.client.model.FileSystemEntry
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File
import java.nio.file.Files
import java.time.Instant

class FireMockDatabase(
    private val fileSystem: FireMockFileSystem
) {
    private val recordsById: MutableMap<String, DbRecord> = mutableMapOf()

    fun saveFile(fileName: String, data: ByteArray): FireApiFile {
        val objectId = Instant.now().nano
        val fireOid = "${objectId}_${fileName.replace("\\s".toRegex(), "_")}"
        val file = fileSystem.saveFile(data, fireOid)
        val fireFile = FireApiFile(objectId, fireOid, file.md5(), file.size(), Instant.now().toString())
        recordsById[fireOid] = DbRecord(fireFile, null, null, false)
        return fireFile
    }

    fun setPath(fireOid: String, path: String) {
        val normalizedPath = "(/*)(.*)".toPattern().match(path)!!.secondGroup()
        val firePath = "/$normalizedPath"
        if (recordsById.values.firstOrNull { it.firePath == firePath } != null)
            throw FireException("Path '$firePath' is already allocated", CONFLICT)

        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(firePath = firePath, fileSystemPath = normalizedPath)
        fileSystem.setPath(fireOid, normalizedPath)
        if (record.published) fileSystem.publish(normalizedPath)
    }

    fun unsetPath(fireOid: String) {
        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(firePath = null, fileSystemPath = null)

        if (record.fileSystemPath != null) {
            fileSystem.unSetPath(record.fileSystemPath)
            fileSystem.unpublish(record.fileSystemPath)
        }
    }

    fun publish(fireOid: String) {
        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(published = true)

        if (record.fileSystemPath != null && record.published.not()) fileSystem.publish(record.fileSystemPath)
    }

    fun unpublish(fireOid: String) {
        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(published = false)
        if (record.fileSystemPath != null) fileSystem.unpublish(record.fileSystemPath)
    }

    fun findByMd5(md5: String): List<FireApiFile> =
        recordsById.values.map { it.toFile() }.filter { it.objectMd5 == md5 }

    fun findByPath(path: String): FireApiFile? = recordsById.values.firstOrNull { it.fileSystemPath == path }?.toFile()

    fun downloadByPath(path: String): File = fileSystem.findFileByPath(path).toFile()

    fun getFile(fireOid: String): File = fileSystem.findFileByFireId(fireOid).toFile()
}

data class DbRecord(
    val file: FireApiFile,
    val firePath: String?,
    val fileSystemPath: String?,
    val published: Boolean
) {
    fun toFile(): FireApiFile = file.copy(filesystemEntry = FileSystemEntry(firePath, published))
}
