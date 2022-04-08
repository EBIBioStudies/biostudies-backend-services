package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.base.remove
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.model.Attribute
import java.nio.file.Path

class GroupSource(groupName: String, private val pathSource: PathFilesSource) : FilesSource by pathSource {

    private val groupPattern = "/?groups/$groupName/".toRegex()

    constructor(groupName: String, path: Path) : this(groupName, PathFilesSource(path))

    override fun getFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>
    ): ExtFile? = pathSource.getFile(path.remove(groupPattern), md5, attributes)
}
