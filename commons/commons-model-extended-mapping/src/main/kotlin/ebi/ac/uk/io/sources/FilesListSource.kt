package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

class FilesListSource(private val files: List<File>) : FilesSource {
    override fun getFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>,
        calculateProperties: Boolean
    ): ExtFile? = files.firstOrNull { it.name == path }?.let { create(path, it, calculateProperties, attributes) }
}
