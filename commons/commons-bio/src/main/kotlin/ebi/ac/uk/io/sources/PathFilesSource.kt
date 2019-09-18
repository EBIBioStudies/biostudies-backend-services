package ebi.ac.uk.io.sources

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class PathFilesSource(private val path: Path) : FilesSource {
    override fun exists(filePath: String) = Files.exists(path.resolve(filePath))

    override fun getFile(filePath: String): File = path.resolve(filePath).toFile()

    override fun size(filePath: String) = path.resolve(filePath).toFile().length()

    override fun readText(filePath: String) = path.resolve(filePath).toFile().readText()
}
