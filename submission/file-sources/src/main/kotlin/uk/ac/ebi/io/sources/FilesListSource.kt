package uk.ac.ebi.io.sources

import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.io.sources.FilesSource
import uk.ac.ebi.io.builder.createFile
import java.io.File

internal class FilesListSource(
    private val files: List<File>,
) : FilesSource {
    override val description: String = "Request files [${files.joinToString { it.name }}]"

    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile? = files.firstOrNull { it.name == path }?.let { createFile(path, it, attributes) }

    override suspend fun getFileList(path: String): File? = files.firstOrNull { it.name == path }
}
