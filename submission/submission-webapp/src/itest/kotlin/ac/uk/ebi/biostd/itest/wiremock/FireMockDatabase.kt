package ac.uk.ebi.biostd.itest.wiremock

import ebi.ac.uk.base.orFalse
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import uk.ac.ebi.fire.client.model.FireApiFile
import uk.ac.ebi.fire.client.model.MetadataEntry
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

class FireMockDatabase(
    private val submissionFolder: Path,
    private val ftpFolder: Path,
    private val dbFolder: Path
) {

    private val records: MutableMap<String, DbRecord> = mutableMapOf()

    fun saveFile(fileName: String, data: ByteArray): FireApiFile {
        val objectId = Instant.now().nano
        val fireOid = "${objectId}_${fileName.replace("\\s".toRegex(), "_")}"
        val file = saveFile(data, fireOid)
        val fireFile = FireApiFile(objectId, fireOid, file.md5(), file.size(), Instant.now().toString())
        records[fireOid] = DbRecord(fireFile, null, false)
        return fireFile
    }

    fun setPath(fireOid: String, path: String) {
        val record = records.getValue(fireOid)
        records[fireOid] = record.copy(path = path)
        val fireFile = dbFolder.resolve(fireOid)

        if (record.path != null) {
            Files.deleteIfExists(submissionFolder.resolve(record.path))
            Files.deleteIfExists(ftpFolder.resolve(record.path))
        }

        Files.copy(fireFile, getOrCreateSubFolder(path))
        if (record.published) Files.copy(fireFile, getOrCreateFtpFolder(path))
    }

    fun unsetPath(fireOid: String) {
        val record = records.getValue(fireOid)
        records[fireOid] = record.copy(path = null)

        if (record.path != null) {
            Files.deleteIfExists(ftpFolder.resolve(record.path))
            Files.delete(submissionFolder.resolve(record.path))
        }
    }

    fun updateMetadata(fireOid: String, entries: List<MetadataEntry>) {
        val file = records.getValue(fireOid)
        val metadata = merge(file.file.metadata.orEmpty(), entries)
        records[fireOid] = file.copy(file = file.file.copy(metadata = metadata))
    }

    fun publish(fireOid: String) {
        val file = records.getValue(fireOid)
        records[fireOid] = file.copy(published = true)

        if (file.path != null && file.published.not()) {
            val source = submissionFolder.resolve(file.path)
            val target = getOrCreateFtpFolder(file.path)
            Files.copy(source, target)
        }
    }

    fun unpublish(fireOid: String) {
        val record = records.getValue(fireOid)
        records[fireOid] = record.copy(published = false)
        if (record.path != null) Files.delete(ftpFolder.resolve(record.path))
    }

    fun findByMetadata(entries: List<MetadataEntry>): List<FireApiFile> =
        records.values.map { it.file }.filter { it.metadata?.containsAll(entries).orFalse() }

    fun findByMd5(md5: String): FireApiFile? = records.values.map { it.file }.firstOrNull { it.objectMd5 == md5 }

    fun findByPath(path: String): FireApiFile? = records.values.firstOrNull { it.path == path }?.file

    fun cleanAll() {
        FileUtils.deleteFile(submissionFolder.toFile())
        FileUtils.deleteFile(ftpFolder.toFile())
        FileUtils.deleteFile(dbFolder.toFile())
        records.clear()
    }

    private fun merge(metadata: List<MetadataEntry>, newKeys: List<MetadataEntry>): List<MetadataEntry> {
        val current = metadata.associateBy { it.key }.toMutableMap()
        newKeys.forEach { current[it.key] = it }
        return current.values.toList()
    }

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
}

data class DbRecord(val file: FireApiFile, val path: String?, val published: Boolean)