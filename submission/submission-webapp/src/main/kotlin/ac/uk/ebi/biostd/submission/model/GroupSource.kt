package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.base.remove
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import java.io.File
import java.nio.file.Path

class GroupSource(groupName: String, private val pathSource: PathFilesSource) : FilesSource by pathSource {

    private val groupPattern = "Groups/$groupName/".toRegex()

    constructor(groupName: String, path: Path) : this(groupName, PathFilesSource(path))

    override fun exists(filePath: String): Boolean = pathSource.exists(filePath.remove(groupPattern))

    override fun getFile(filePath: String): File = pathSource.getFile(filePath.remove(groupPattern))

    override fun size(filePath: String) = pathSource.size(filePath.remove(groupPattern))

    override fun readText(filePath: String) = pathSource.readText(filePath.remove(groupPattern))
}
