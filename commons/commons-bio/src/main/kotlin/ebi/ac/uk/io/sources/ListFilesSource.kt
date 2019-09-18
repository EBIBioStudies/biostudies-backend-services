package ebi.ac.uk.io.sources

import java.io.File

class ListFilesSource(private val files: List<File>) : FilesSource {
    override fun exists(filePath: String) = files.any { it.name == filePath }

    override fun getFile(filePath: String): File = files.first { it.name == filePath }

    override fun size(filePath: String) = files.first { it.name == filePath }.length()

    override fun readText(filePath: String) = files.first { it.name == filePath }.readText()
}
