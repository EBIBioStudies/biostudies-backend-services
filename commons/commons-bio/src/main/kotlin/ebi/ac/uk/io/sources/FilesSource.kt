package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

enum class PreferredSource { FIRE, SUBMISSION, USER_SPACE }

interface FilesSource {
    val description: String

    fun getExtFile(path: String, fileDb: FileDb? = null, attributes: List<Attribute> = emptyList()): ExtFile?
    fun getFile(path: String, fileDb: FileDb? = null): File?
}

sealed interface FileDb {
    val md5: String
}

data class UploadedFileDb(override val md5: String) : FileDb
data class ConfiguredFileDb(val id: String, override val md5: String, val path: String, val size: Long) : FileDb
