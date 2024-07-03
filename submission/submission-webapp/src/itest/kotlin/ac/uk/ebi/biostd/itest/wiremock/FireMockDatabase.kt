package ac.uk.ebi.biostd.itest.wiremock

import ac.uk.ebi.biostd.itest.wiremock.handlers.FireException
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import uk.ac.ebi.fire.client.model.FileSystemEntry
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class FireMockDatabase(
    private val fileSystem: FireMockFileSystem,
) {
    private val recordsById: MutableMap<String, DbRecord> = ConcurrentHashMap<String, DbRecord>()

    fun saveFile(
        fileName: String,
        data: ByteArray,
    ): FireApiFile {
        val objectId = Instant.now().nano
        val fireOid = "${objectId}_${fileName.replace("\\s".toRegex(), "_")}"
        val file = fileSystem.saveFile(data, fireOid)
        val fireFile = FireApiFile(objectId, fireOid, file.md5(), file.size(), Instant.now().toString())
        recordsById[fireOid] = DbRecord(fireFile, null, false)
        return fireFile
    }

    fun findByPath(firePath: String): FireApiFile {
        return recordsById.values
            .firstOrNull { it.firePath == firePath }?.toFile()
            ?: throw FireException("File $firePath was not found", NOT_FOUND)
    }

    fun setPath(
        fireOid: String,
        firePath: String,
    ): FireApiFile {
        if (recordsById.values.any { it.firePath == firePath }) {
            throw FireException("Path '$firePath' is already allocated", CONFLICT)
        }

        val record = recordsById.getValue(fireOid).copy(firePath = firePath)
        recordsById[fireOid] = record
        fileSystem.setPath(fireOid, firePath)

        if (record.published) fileSystem.publish(firePath)

        return record.toFile()
    }

    fun unsetPath(fireOid: String) {
        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(firePath = null)

        if (record.firePath != null) {
            fileSystem.delete(record.firePath)
        }
    }

    fun delete(fireOid: String) {
        val record = recordsById.getValue(fireOid)
        if (record.firePath != null) {
            fileSystem.delete(record.firePath)
        }

        recordsById.remove(fireOid)
    }

    fun publish(fireOid: String): FireApiFile {
        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(published = true)
        if (record.firePath != null && record.published.not()) fileSystem.publish(record.firePath)
        return recordsById.getValue(fireOid).toFile()
    }

    fun unpublish(fireOid: String): FireApiFile {
        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(published = false)
        if (record.firePath != null) fileSystem.unpublish(record.firePath)
        return recordsById.getValue(fireOid).toFile()
    }

    fun findByMd5(md5: String): List<FireApiFile> = recordsById.values.map { it.toFile() }.filter { it.objectMd5 == md5 }

    fun getFile(fireOid: String): File = fileSystem.findFileByFireId(fireOid).toFile()
}

data class DbRecord(
    val file: FireApiFile,
    val firePath: String?,
    val published: Boolean,
) {
    // The path is normalized to ALWAYS include an initial slash in order to mimic how FIRE's HTTP endpoint
    fun toFile(): FireApiFile = file.copy(filesystemEntry = FileSystemEntry(firePath?.let { "/$it" }, published))
}
