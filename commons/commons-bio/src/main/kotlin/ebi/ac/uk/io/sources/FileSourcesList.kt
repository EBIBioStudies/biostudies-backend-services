package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

@JvmInline
value class FileSourcesList(val sources: List<FilesSource>) {
    fun findExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        return sources.firstNotNullOfOrNull { it.getExtFile(path, type, attributes) }
    }

    fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile {
        return findExtFile(path, type, attributes) ?: throw FilesProcessingException(path, this)
    }

    fun getFileList(path: String): File? {
        return sources.firstNotNullOfOrNull { it.getFileList(path) }
    }
}
