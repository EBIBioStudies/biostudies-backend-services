package ebi.ac.uk.io.sources

import java.nio.file.Files
import java.nio.file.Path

class PathFilesSource(private val path: Path) : FilesSource {
    override fun exists(filePath: String) = Files.exists(path.resolve(filePath))

    override fun getFile(filePath: String): NfsBioFile = NfsBioFile(path.resolve(filePath).toFile())
}
