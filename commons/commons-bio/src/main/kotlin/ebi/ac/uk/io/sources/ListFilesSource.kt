package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException
import java.io.File

class ListFilesSource(private val files: List<File>) : FilesSource {
    override fun exists(filePath: String) = files.any { it.name == filePath }

    override fun getFile(filePath: String): NfsBioFile {
        val file = files.firstOrNull { it.name == filePath } ?: throw FileNotFoundException(filePath)
        return NfsBioFile(file)
    }
}
