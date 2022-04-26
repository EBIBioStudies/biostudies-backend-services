package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

class FilesListSource(
    private val files: List<File>,
    override val filesOrigin: FileOrigin
) : FilesSource {
    override fun getExtFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>
    ): ExtFile? = files.firstOrNull { it.name == path }?.let { create(path, it, attributes) }

    override fun getFile(path: String, md5: String?): File? = files.firstOrNull { it.name == path }
}
