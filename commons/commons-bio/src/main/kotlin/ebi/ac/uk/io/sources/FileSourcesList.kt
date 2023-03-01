package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

@JvmInline
value class FileSourcesList(val sources: List<FilesSource>) {
    fun getExtFile(
        path: String,
        attributes: List<Attribute>,
    ): ExtFile? = sources.firstNotNullOfOrNull { it.getExtFile(path, attributes) }

    fun getFile(path: String): File? = sources.firstNotNullOfOrNull { it.getFile(path) }
}
