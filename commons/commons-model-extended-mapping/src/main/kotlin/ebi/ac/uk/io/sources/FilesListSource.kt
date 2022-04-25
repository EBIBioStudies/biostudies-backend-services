package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileOrigin
import ebi.ac.uk.model.Attribute
import java.io.File

class FilesListSource(
    private val files: List<File>,
    override val filesOrigin: ExtFileOrigin
) : FilesSource {
    override fun getExtFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>,
        preferredOrigin: ExtFileOrigin
    ): ExtFile? = files.firstOrNull { it.name == path }?.let { create(path, it, attributes, filesOrigin) }

    override fun getFile(path: String, md5: String?): File? = files.firstOrNull { it.name == path }
}
