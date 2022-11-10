package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

class FilesListSource(private val files: List<File>) : FilesSource {
    override val description: String = "Request files [${files.joinToString { it.name }}]"

    override fun getExtFile(path: String, dbFile: DbFile?, attributes: List<Attribute>): ExtFile? =
        files.firstOrNull { it.name == path }?.let { create(path, it, attributes) }

    override fun getFile(path: String): File? = files.firstOrNull { it.name == path }
}
