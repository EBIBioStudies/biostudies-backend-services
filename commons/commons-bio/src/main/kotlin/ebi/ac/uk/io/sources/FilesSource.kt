package ebi.ac.uk.io.sources

import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import java.io.File

interface FilesSource {
    fun exists(filePath: String): Boolean

    fun getFile(filePath: String): BioFile
}

sealed class BioFile {
    abstract fun readContent(): String
    abstract fun md5(): String
    abstract fun size(): Long
}

class NfsBioFile(val file: File) : BioFile() {
    override fun readContent(): String = file.readText()
    override fun md5(): String = file.md5()
    override fun size(): Long = file.size()
}

class FireBioFile(val fireId: String, val md5: String) : BioFile() {
    override fun readContent(): String = TODO()
    override fun md5(): String = TODO()
    override fun size(): Long = TODO()
}

