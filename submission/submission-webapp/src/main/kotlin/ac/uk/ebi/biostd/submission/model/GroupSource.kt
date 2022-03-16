package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.base.remove
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.io.sources.PathFilesSource
import java.nio.file.Path

class GroupSource(groupName: String, private val pathSource: PathFilesSource) : FilesSource by pathSource {

    private val groupPattern = "/?groups/$groupName/".toRegex()

    constructor(groupName: String, path: Path) : this(groupName, PathFilesSource(path, null))

    override fun exists(filePath: String): Boolean = pathSource.exists(filePath.remove(groupPattern))

    override fun getFile(filePath: String): NfsBioFile = pathSource.getFile(filePath.remove(groupPattern))
}
