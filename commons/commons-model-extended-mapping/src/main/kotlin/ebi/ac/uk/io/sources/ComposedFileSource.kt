package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute

class ComposedFileSource(private val sources: List<FilesSource>) : FilesSource {
    override fun getFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>,
        calculateProperties: Boolean
    ): ExtFile? = sources.firstNotNullOfOrNull { it.getFile(path, md5, attributes, calculateProperties) }
}
