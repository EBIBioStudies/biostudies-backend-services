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
import java.nio.file.Path
import java.time.Instant

class FireMockDatabase(
    private val submissionFolder: Path,
    private val ftpFolder: Path,
    private val dbFolder: Path,
) {
    private val recordsById: MutableMap<String, DbRecord> = mutableMapOf()

    fun saveFile(fileName: String, data: ByteArray): FireApiFile {
        val objectId = Instant.now().nano
        val fireOid = "${objectId}_${fileName.replace("\\s".toRegex(), "_")}"
        val file = saveFile(data, fireOid)
        val fireFile = FireApiFile(objectId, fireOid, file.md5(), file.size(), Instant.now().toString())
        recordsById[fireOid] = DbRecord(fireFile, null, false)
        return fireFile
    }

    fun setPath(fireOid: String, path: String) {
        val normalizedPath = normalizePath(path)
        val firePath = "/$normalizedPath"
        if (recordsById.values.firstOrNull { it.path == firePath } != null)
            throw FireException("Path '$firePath' is already allocated", CONFLICT)

        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(path = firePath)
        val fireFile = dbFolder.resolve(fireOid)

        Files.copy(fireFile, getOrCreateSubFolder(normalizedPath))
        if (record.published) Files.copy(fireFile, getOrCreateFtpFolder(normalizedPath))
    }

    fun unsetPath(fireOid: String) {
        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(path = null)

        if (record.path != null) {
            val normalizedPath = normalizePath(record.path)
            Files.deleteIfExists(ftpFolder.resolve(normalizedPath))
            Files.delete(submissionFolder.resolve(normalizedPath))
        }
    }

    fun publish(fireOid: String) {
        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(published = true)

        if (record.path != null && record.published.not()) {
            val normalizedPath = normalizePath(record.path)
            val source = submissionFolder.resolve(normalizedPath)
            val target = getOrCreateFtpFolder(normalizedPath)
            Files.copy(source, target)
        }
    }

    fun unpublish(fireOid: String) {
        val record = recordsById.getValue(fireOid)
        recordsById[fireOid] = record.copy(published = false)
        if (record.path != null) Files.delete(ftpFolder.resolve(normalizePath(record.path)))
    }

    fun findByMd5(md5: String): List<FireApiFile> =
        recordsById.values.map { it.toFile() }.filter { it.objectMd5 == md5 }

    fun findByPath(path: String): FireApiFile? {
        return recordsById.values.firstOrNull { it.path == "/${normalizePath(path)}" }?.toFile()
    }

    fun downloadByPath(path: String): File = submissionFolder.resolve(normalizePath(path)).toFile()

    private fun saveFile(data: ByteArray, fireOid: String): File {
        val file = dbFolder.resolve(fireOid).toFile()
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeBytes(data)
        return file
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

    fun getFile(fireOid: String): File {
        return dbFolder.resolve(fireOid).toFile()
    }

    private fun normalizePath(path: String) = "(/*)(.*)".toPattern().match(path)!!.secondGroup()
}

data class DbRecord(val file: FireApiFile, val path: String?, val published: Boolean) {
    fun toFile(): FireApiFile = file.copy(filesystemEntry = FileSystemEntry(path, published))
}
