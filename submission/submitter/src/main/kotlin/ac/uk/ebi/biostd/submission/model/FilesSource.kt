package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.base.fold
import java.nio.file.Files
import java.nio.file.Path

class FilesSource(attached: List<ResourceFile>, userFilesDir: Path) {
    private val attachedFiles = AttachedFilesSource(attached)
    private val userFiles = UserFilesSource(userFilesDir)

    fun exists(filePath: String) = attachedFiles.exists(filePath).or(userFiles.exists(filePath))

    fun getInputStream(filePath: String) =
        attachedFiles.exists(filePath).fold(
            { attachedFiles.getInputStream(filePath) }, { userFiles.getInputStream(filePath) })

    fun size(filePath: String) =
        attachedFiles.exists(filePath).fold({ attachedFiles.size(filePath) }, { userFiles.size(filePath) })

    fun readText(filePath: String) =
        attachedFiles.exists(filePath).fold({ attachedFiles.readText(filePath) }, { userFiles.readText(filePath) })
}

class AttachedFilesSource(private val files: List<ResourceFile>) {
    fun exists(filePath: String) = files.any { it.name == filePath }

    fun getInputStream(filePath: String) = files.first { it.name == filePath }.inputStream

    fun size(filePath: String) = files.first { it.name == filePath }.size

    fun readText(filePath: String) = files.first { it.name == filePath }.text
}

class UserFilesSource(private val path: Path) {
    fun exists(filePath: String) = Files.exists(path.resolve(filePath))

    fun getInputStream(filePath: String) = path.resolve(filePath).toFile().inputStream()

    fun size(filePath: String) = path.resolve(filePath).toFile().totalSpace

    fun readText(filePath: String) = path.resolve(filePath).toFile().readText()
}
