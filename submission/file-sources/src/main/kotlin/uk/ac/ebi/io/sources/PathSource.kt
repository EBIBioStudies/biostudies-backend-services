package uk.ac.ebi.io.sources

import ebi.ac.uk.base.remove
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.constants.FileFields.DIRECTORY_TYPE
import uk.ac.ebi.io.builder.createFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

internal class PathSource(
    override val description: String,
    private val sourcePath: Path,
) : FilesSource {
    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile? = findFile(path)?.let { createFile(path, it, attributes) }

    override suspend fun getFileList(path: String): File? = findFile(path)

    private fun findFile(path: String): File? {
        val filePath = sourcePath.resolve(path)
        return if (Files.exists(filePath)) filePath.toFile() else null
    }
}

/**
 *  File system directory source. Note that file type and file extension is check in case file is generated zip file
 *  of a folder.
 */
internal class UserPathSource(
    override val description: String,
    sourcePath: Path,
) : FilesSource {
    private val pathSource = PathSource(description, sourcePath)

    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile? {
        val filePath = if (type == DIRECTORY_TYPE.value) path.removeSuffix(".zip") else path
        return pathSource.getExtFile(filePath, type, attributes)
    }

    override suspend fun getFileList(path: String): File? = pathSource.getFileList(path)
}

internal class GroupPathSource(
    groupName: String,
    private val pathSource: PathSource,
) : FilesSource by pathSource {
    private val groupPattern = "/?groups/$groupName/".toRegex()

    constructor(groupName: String, path: Path) : this(groupName, PathSource("Group '$groupName' files", path))

    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile? = pathSource.getExtFile(path.remove(groupPattern), type, attributes)
}
