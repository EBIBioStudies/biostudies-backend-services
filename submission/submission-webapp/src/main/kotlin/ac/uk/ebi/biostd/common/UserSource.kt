package ac.uk.ebi.biostd.common

import ebi.ac.uk.base.fold
import ebi.ac.uk.utils.FilesSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

// TODO: Add group sources
class UserSource(attached: List<File>, userFilesDir: Path) : FilesSource {

    private val attachedFiles = AttachedFilesSource(attached)
    private val userFiles = LocalDirectoryFilesSource(userFilesDir)

    override fun exists(filePath: String) = attachedFiles.exists(filePath).or(userFiles.exists(filePath))

    override fun getFile(filePath: String) =
        attachedFiles.exists(filePath).fold({ attachedFiles.getFile(filePath) }, { userFiles.getFile(filePath) })

    fun size(filePath: String): Long =
        attachedFiles.exists(filePath).fold({ attachedFiles.size(filePath) }, { userFiles.size(filePath) })

    fun readText(filePath: String) =
        attachedFiles.exists(filePath).fold({ attachedFiles.readText(filePath) }, { userFiles.readText(filePath) })
}

class AttachedFilesSource(private val files: List<File>) {
    fun exists(filePath: String) = files.any { it.name == filePath }

    fun getFile(filePath: String): File = files.first { it.name == filePath }

    fun size(filePath: String) = files.first { it.name == filePath }.length()

    fun readText(filePath: String) = files.first { it.name == filePath }.readText()
}

class LocalDirectoryFilesSource(private val path: Path) {
    fun exists(filePath: String) = Files.exists(path.resolve(filePath))

    fun getFile(filePath: String): File = path.resolve(filePath).toFile()

    fun size(filePath: String) = path.resolve(filePath).toFile().length()

    fun readText(filePath: String) = path.resolve(filePath).toFile().readText()
}
