package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

class ComposedFileSource(private val sources: List<FilesSource>) : FilesSource {
    override fun getExtFile(path: String, md5: String?, attributes: List<Attribute>): ExtFile? =
        sources.firstNotNullOfOrNull { it.getExtFile(path, md5, attributes) }

    override fun getFile(path: String, md5: String?): File? =
        sources.firstNotNullOfOrNull { it.getFile(path, md5) }
}
