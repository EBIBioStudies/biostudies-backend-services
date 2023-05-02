package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

@JvmInline
value class FileSourcesList(val sources: List<FilesSource>) {
    fun getExtFile(
        path: String,
        type: String,
        attributes: List<Attribute>,
    ): ExtFile? = sources.firstNotNullOfOrNull { it.getExtFile(path, type, attributes) }

    fun getFileList(path: String): File? = sources.firstNotNullOfOrNull { it.getFileList(path) }
}
