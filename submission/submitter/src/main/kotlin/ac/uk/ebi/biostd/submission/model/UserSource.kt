package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.base.fold
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.ListFilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import java.io.File
import java.nio.file.Path

// TODO: Add group sources
class UserSource(attached: List<File>, userFilesDir: Path) : FilesSource {

    private val attachedFiles = ListFilesSource(attached)
    private val userFiles = PathFilesSource(userFilesDir)

    override fun exists(filePath: String) = attachedFiles.exists(filePath).or(userFiles.exists(filePath))

    override fun getFile(filePath: String) =
        attachedFiles.exists(filePath).fold({ attachedFiles.getFile(filePath) }, { userFiles.getFile(filePath) })

    override fun size(filePath: String): Long =
        attachedFiles.exists(filePath).fold({ attachedFiles.size(filePath) }, { userFiles.size(filePath) })

    override fun readText(filePath: String) =
        attachedFiles.exists(filePath).fold({ attachedFiles.readText(filePath) }, { userFiles.readText(filePath) })
}
