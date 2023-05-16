package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields.DIRECTORY_TYPE
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class PathSource(
    override val description: String,
    private val sourcePath: Path,
) : FilesSource {
    override fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? =
        findFile(path)?.let { create(path, it, attributes) }

    override fun getFileList(path: String): File? = findFile(path)

    private fun findFile(path: String): File? {
        val filePath = sourcePath.resolve(path)
        return if (Files.exists(filePath)) filePath.toFile() else null
    }
}

/**
 *  File system directory source. Note that file type and file extension is check in case file is generated zip file
 *  of a folder.
 */
class UserPathSource(override val description: String, sourcePath: Path) : FilesSource {

    private val pathSource = PathSource(description, sourcePath)

    override fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        val filePath = when (type) {
            DIRECTORY_TYPE.value -> path.removeSuffix(".zip")
            else -> path
        }
        return pathSource.getExtFile(filePath, type, attributes)
    }

    override fun getFileList(path: String): File? = pathSource.getFileList(path)
}
