package uk.ac.ebi.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import uk.ac.ebi.io.builder.createFile
import java.io.File

class FilesListSource(private val files: List<File>) : FilesSource {
    override val description: String = "Request files [${files.joinToString { it.name }}]"

    override fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        return files.firstOrNull { it.name == path }?.let { createFile(path, it, attributes) }
    }

    override fun getFileList(path: String): File? = files.firstOrNull { it.name == path }
}
