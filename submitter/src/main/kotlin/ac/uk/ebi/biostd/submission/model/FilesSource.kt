package ac.uk.ebi.biostd.submission.model

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

interface FilesSource {

    fun exists(filePath: String): Boolean

    fun getInputStream(filePath: String): InputStream

    fun size(filePath: String): Long
}

class ListFilesSource(private val files: List<ResourceFile>) : FilesSource {

    override fun exists(filePath: String) = files.any { it.name == filePath }

    override fun getInputStream(filePath: String) = files.first { it.name == filePath }.inputStream

    override fun size(filePath: String) = files.first { it.name == filePath }.size

}

class PathFilesSource(private val path: Path) : FilesSource {

    override fun exists(filePath: String) = Files.exists(path.resolve(filePath))

    override fun getInputStream(filePath: String) = path.resolve(filePath).toFile().inputStream()

    override fun size(filePath: String) = path.resolve(filePath).toFile().totalSpace
}
