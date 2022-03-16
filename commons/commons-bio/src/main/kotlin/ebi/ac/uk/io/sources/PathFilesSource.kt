package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

class PathFilesSource(
    private val path: Path,
    override val rootPath: String?
) : FilesSource {
    override fun exists(filePath: String) = Files.exists(path.resolve(filePath))

    override fun getFile(filePath: String): NfsBioFile {
        val file = path.resolve(filePath).toFile()
        require(file.exists()) { throw FileNotFoundException(fullPath(filePath)) }

        return NfsBioFile(file)
    }
}
