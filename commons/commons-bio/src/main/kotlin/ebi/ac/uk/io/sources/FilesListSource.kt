package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException
import java.io.File

class FilesListSource(
    private val files: List<File>,
    override val rootPath: String?
) : FilesSource {
    override fun exists(filePath: String) = files.any { it.name == filePath }

    override fun getFile(filePath: String): NfsBioFile {
        val file = files.firstOrNull { it.name == filePath } ?: throw FileNotFoundException(fullPath(filePath))
        return NfsBioFile(file)
    }
}
