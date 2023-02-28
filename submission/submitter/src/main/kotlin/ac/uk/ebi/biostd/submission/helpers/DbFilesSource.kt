package ac.uk.ebi.biostd.submission.helpers

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.ConfiguredDbFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields.DB_ID
import ebi.ac.uk.model.constants.FileFields.DB_MD5
import ebi.ac.uk.model.constants.FileFields.DB_PATH
import ebi.ac.uk.model.constants.FileFields.DB_PUBLISHED
import ebi.ac.uk.model.constants.FileFields.DB_SIZE
import java.io.File

/**
 * Source that allows the submitter to use store files directly in fire and bypass backend mechanism.
 */
object DbFilesSource : FilesSource {
    override val description: String
        get() = "Provided Db files"

    override fun getExtFile(path: String, attributes: List<Attribute>): ExtFile? {
        val valuesMap = attributes.associateBy({ it.name }, { it.value })
        val dbFile = getDbFile(valuesMap)
        return if (dbFile != null) return asFireFile(path, dbFile, attributes) else null
    }

    override fun getFile(path: String): File? = null

    @Suppress("ComplexCondition")
    private fun getDbFile(attributes: Map<String, String?>): ConfiguredDbFile? {
        val md5 = attributes[DB_MD5.value]
        val size = attributes[DB_SIZE.value]
        val id = attributes[DB_ID.value]
        val path = attributes[DB_PATH.value]
        val published = attributes[DB_PUBLISHED.value]
        return if (md5 == null || size == null || id == null || path == null) null
        else ConfiguredDbFile(id, md5, path, size.toLong(), published.toBoolean())
    }
}
