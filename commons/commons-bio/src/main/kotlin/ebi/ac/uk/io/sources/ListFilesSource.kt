package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException
import java.io.File

class ListFilesSource(private val files: List<File>) : FilesSource {
    override fun exists(filePath: String) = files.any { it.name == filePath }

    override fun getFile(filePath: String): File =
        files.firstOrNull { it.name == filePath } ?: throw FileNotFoundException(filePath)

    override fun readText(filePath: String) = getFile(filePath).readText()
}
