package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

enum class PreferredSource { FIRE, SUBMISSION, USER_SPACE }

interface FilesSource {
    val description: String

    fun getExtFile(path: String, dbFile: DbFile? = null, attributes: List<Attribute> = emptyList()): ExtFile?
    fun getFile(path: String, dbFile: DbFile? = null): File?
}

sealed interface DbFile {
    val md5: String
}

data class UploadedDbFile(override val md5: String) : DbFile
data class ConfiguredDbFile(val id: String, override val md5: String, val path: String, val size: Long) : DbFile
