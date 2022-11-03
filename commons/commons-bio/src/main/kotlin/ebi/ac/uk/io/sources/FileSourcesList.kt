package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

@JvmInline
value class FileSourcesList(val sources: List<FilesSource>) {
    fun getExtFile(
        path: String,
        dbFile: DbFile? = null,
        attributes: List<Attribute> = emptyList()
    ): ExtFile? = sources.firstNotNullOfOrNull { it.getExtFile(path, dbFile, attributes) }

    fun getFile(path: String): File? = sources.firstNotNullOfOrNull { it.getFile(path) }
}
