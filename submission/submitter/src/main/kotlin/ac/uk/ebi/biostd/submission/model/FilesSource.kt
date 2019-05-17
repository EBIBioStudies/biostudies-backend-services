package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.base.fold
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
    private val attachedFiles: AttachedFilesSource,
    private val userFiles: UserFilesSource
) : FilesSource {
    override fun exists(filePath: String) = attachedFiles.exists(filePath).or(userFiles.exists(filePath))

    override fun getInputStream(filePath: String) =
        attachedFiles.exists(filePath).fold(
            { attachedFiles.getInputStream(filePath) }, { userFiles.getInputStream(filePath) })

    override fun size(filePath: String) =
        attachedFiles.exists(filePath).fold({ attachedFiles.size(filePath) }, { userFiles.size(filePath) })

    override fun readText(filePath: String) =
        attachedFiles.exists(filePath).fold({ attachedFiles.readText(filePath) }, { userFiles.readText(filePath) })
}

class AttachedFilesSource(private val files: List<ResourceFile>) : FilesSource {
    override fun exists(filePath: String) = files.any { it.name == filePath }

    override fun getInputStream(filePath: String) = files.first { it.name == filePath }.inputStream

    override fun size(filePath: String) = files.first { it.name == filePath }.size

    override fun readText(filePath: String) = files.first { it.name == filePath }.text
}

class UserFilesSource(private val path: Path) : FilesSource {
    override fun exists(filePath: String) = Files.exists(path.resolve(filePath))

    override fun getInputStream(filePath: String) = path.resolve(filePath).toFile().inputStream()

    override fun size(filePath: String) = path.resolve(filePath).toFile().totalSpace

    override fun readText(filePath: String) = path.resolve(filePath).toFile().readText()
}
