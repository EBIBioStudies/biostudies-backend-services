package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.base.remove
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.DbFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathSource
import ebi.ac.uk.model.Attribute
import java.nio.file.Path

class GroupSource(groupName: String, private val pathSource: PathSource) : FilesSource by pathSource {

    private val groupPattern = "/?groups/$groupName/".toRegex()

    constructor(groupName: String, path: Path) : this(groupName, PathSource("Group '$groupName' files", path))

    override fun getExtFile(
        path: String,
        dbFile: DbFile?,
        attributes: List<Attribute>,
    ): ExtFile? = pathSource.getExtFile(path.remove(groupPattern), dbFile, attributes)
}
