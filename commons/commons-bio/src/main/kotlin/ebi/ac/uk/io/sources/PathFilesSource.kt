package ebi.ac.uk.io.sources

import java.nio.file.Files
import java.nio.file.Path

class PathFilesSource(private val sourcePath: Path) : FilesSource {
    override fun getFile(path: String, md5: String?): NfsBioFile? {
        val filePath = sourcePath.resolve(path)
        return if (Files.exists(filePath)) NfsBioFile(filePath.toFile()) else null
    }
}
