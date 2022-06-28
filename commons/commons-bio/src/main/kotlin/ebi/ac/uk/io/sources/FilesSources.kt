package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

class FilesSources(val sources: List<FilesSource>) {
    fun getExtFile(path: String, md5: String? = null, attributes: List<Attribute> = emptyList()): ExtFile? =
        sources.firstNotNullOfOrNull { it.getExtFile(path, md5, attributes) }

    fun getFile(path: String, md5: String? = null): File? =
        sources.firstNotNullOfOrNull { it.getFile(path, md5) }
}
