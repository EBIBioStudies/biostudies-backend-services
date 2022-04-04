package ac.uk.ebi.biostd.itest.wiremock

import ebi.ac.uk.base.orFalse
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import uk.ac.ebi.fire.client.model.FireFile
import uk.ac.ebi.fire.client.model.MetadataEntry
import java.io.File
import java.nio.file.Files
import java.time.Instant

class FireMockDatabase(
    private val submissionFolder: File,
    private val ftpFolder: File,
    private val dbFolder: File
) {

    private val records: MutableMap<String, DbRecord> = mutableMapOf()

    fun saveFile(fileName: String, data: ByteArray): FireFile {
        val objectId = Instant.now().nano
        val fireOid = "${objectId}_${fileName.replace("\\s".toRegex(), "_")}"
        val file = saveFile(data, fireOid)
        val fireFile = FireFile(objectId, fireOid, file.md5(), file.size(), Instant.now().toString())
        records[fireOid] = DbRecord(fireFile, null, false)
        return fireFile
    }

    fun setPath(fireOid: String, path: String) {
        val record = records.getValue(fireOid)
        records[fireOid] = record.copy(path = path)
        val fireFile = dbFolder.resolve(fireOid)

        if (record.path != null) {
            Files.deleteIfExists(submissionFolder.resolve(record.path).toPath())
            Files.deleteIfExists(ftpFolder.resolve(record.path).toPath())
        }

        Files.copy(fireFile.toPath(), getOrCreateSubFolder(path).toPath())
        if (record.published) Files.copy(fireFile.toPath(), getOrCreateFtpFolder(path).toPath())
    }

    fun unsetPath(fireOid: String) {
        val record = records.getValue(fireOid)
        records[fireOid] = record.copy(path = null)

        if (record.path != null) {
            Files.deleteIfExists(ftpFolder.resolve(record.path).toPath())
            Files.delete(submissionFolder.resolve(record.path).toPath())
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

        if (file.path != null) {
            val source = submissionFolder.resolve(file.path)
            val target = getOrCreateFtpFolder(file.path)
            Files.copy(source.toPath(), target.toPath())
        }
    }

    fun unpublish(fireOid: String) {
        val record = records.getValue(fireOid)
        records[fireOid] = record.copy(published = false)
        if (record.path != null) Files.delete(ftpFolder.resolve(record.path).toPath())
    }

    fun findByMetadata(entries: List<MetadataEntry>): List<FireFile> =
        records.values.map { it.file }.filter { it.metadata?.containsAll(entries).orFalse() }

    fun findByMd5(md5: String): FireFile? = records.values.map { it.file }.firstOrNull { it.objectMd5 == md5 }

    fun findByPath(path: String): FireFile? = records.values.firstOrNull { it.path == path }?.file

    fun cleanAll() = records.clear()

    private fun merge(metadata: List<MetadataEntry>, newKeys: List<MetadataEntry>): List<MetadataEntry> {
        val current = metadata.associateBy { it.key }.toMutableMap()
        newKeys.forEach { current[it.key] = it }
        return current.values.toList()
    }

    private fun saveFile(data: ByteArray, fireOid: String): File {
        val file = dbFolder.resolve(fireOid)
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeBytes(data)
        return file
    }

    private fun getOrCreateSubFolder(relPath: String): File {
        val file = submissionFolder.resolve(relPath)
        file.parentFile.mkdirs()
        return file
    }

    private fun getOrCreateFtpFolder(path: String): File {
        val file = ftpFolder.resolve(path)
        file.parentFile.mkdirs()
        return file
    }
}

data class DbRecord(val file: FireFile, val path: String?, val published: Boolean)
