package ac.uk.ebi.biostd.submission.helpers

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.ConfiguredDbFile
import ebi.ac.uk.io.sources.DbFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import java.io.File

/**
 * Source that allows submitted to use store files directly in fire and bypass backend mechanism.
 */
object DbFilesSource : FilesSource {
    override val description: String
        get() = "Provided Db files"

    override fun getExtFile(path: String, dbFile: DbFile?, attributes: List<Attribute>): ExtFile? {
        return if (dbFile is ConfiguredDbFile) return asFireFile(path, dbFile, attributes) else null
    }

    override fun getFile(path: String): File? = null
}
