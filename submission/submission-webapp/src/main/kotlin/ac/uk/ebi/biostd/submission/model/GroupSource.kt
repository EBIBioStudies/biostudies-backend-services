package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.base.remove
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FileOrigin
import ebi.ac.uk.io.sources.FileOrigin.GROUP_SPACE
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.model.Attribute
import java.nio.file.Path

class GroupSource(
    groupName: String,
    private val pathSource: PathFilesSource
) : FilesSource by pathSource {
    private val groupPattern = "/?groups/$groupName/".toRegex()

    override val filesOrigin: FileOrigin
        get() = GROUP_SPACE

    constructor(groupName: String, path: Path) : this(groupName, PathFilesSource(path, GROUP_SPACE))

    override fun getExtFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>
    ): ExtFile? = pathSource.getExtFile(path.remove(groupPattern), md5, attributes)
}
