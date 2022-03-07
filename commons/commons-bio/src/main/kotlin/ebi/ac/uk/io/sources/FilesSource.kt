package ebi.ac.uk.io.sources

import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import java.io.File

interface FilesSource {
    fun exists(filePath: String): Boolean

    fun getFile(filePath: String): BioFile
}

sealed class BioFile {
    abstract fun fileName(): String
    abstract fun readContent(): String
    abstract fun md5(): String
    abstract fun size(): Long
}

class NfsBioFile(val file: File) : BioFile() {
    override fun fileName(): String = file.name
    override fun readContent(): String = file.readText()
    override fun md5(): String = file.md5()
    override fun size(): Long = file.size()
}

class FireBioFile(
    val fireId: String,
    val fileName: String,
    val md5: String,
    val size: Long,
    private val readContent: Lazy<String>,
) : BioFile() {
    override fun fileName(): String = fileName
    override fun readContent(): String = readContent.value
    override fun md5(): String = md5
    override fun size(): Long = size
}

class FireDirectoryBioFile(
    val fileName: String,
    val md5: String,
    val size: Long
) : BioFile() {
    override fun fileName(): String = fileName
    override fun md5(): String = md5
    override fun size(): Long = size
    override fun readContent(): String = throw UnsupportedOperationException()
}
