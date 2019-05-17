package ac.uk.ebi.biostd.submission.model

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

interface FilesSource {
    fun exists(filePath: String): Boolean

    fun getInputStream(filePath: String): InputStream

    fun size(filePath: String): Long

    fun readText(filePath: String): String
}

class MixedFilesSource(
    private val listFiles: ListFilesSource,
    private val pathFiles: PathFilesSource
) : FilesSource {
    override fun exists(filePath: String) = listFiles.exists(filePath).or(pathFiles.exists(filePath))

    override fun getInputStream(filePath: String) =
        executeBySource(filePath, { listFiles.getInputStream(filePath) }, { pathFiles.getInputStream(filePath) })

    override fun size(filePath: String) =
        executeBySource(filePath, { listFiles.size(filePath) }, { pathFiles.size(filePath) })

    override fun readText(filePath: String) =
        executeBySource(filePath, { listFiles.readText(filePath) }, { pathFiles.readText(filePath) })

    private fun <T> executeBySource(filePath: String, asListFile: () -> T, asPathFile: () -> T): T =
        if (listFiles.exists(filePath)) asListFile() else asPathFile()
}

class ListFilesSource(private val files: List<ResourceFile>) : FilesSource {
    override fun exists(filePath: String) = files.any { it.name == filePath }

    override fun getInputStream(filePath: String) = files.first { it.name == filePath }.inputStream

    override fun size(filePath: String) = files.first { it.name == filePath }.size

    override fun readText(filePath: String) = files.first { it.name == filePath }.text
}

class PathFilesSource(private val path: Path) : FilesSource {
    override fun exists(filePath: String) = Files.exists(path.resolve(filePath))

    override fun getInputStream(filePath: String) = path.resolve(filePath).toFile().inputStream()

    override fun size(filePath: String) = path.resolve(filePath).toFile().totalSpace

    override fun readText(filePath: String) = path.resolve(filePath).toFile().readText()
}
