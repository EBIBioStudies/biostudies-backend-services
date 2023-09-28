package uk.ac.ebi.io.sources

import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS
import ebi.ac.uk.model.constants.FileFields.DB_ID
import ebi.ac.uk.model.constants.FileFields.DB_MD5
import ebi.ac.uk.model.constants.FileFields.DB_PATH
import ebi.ac.uk.model.constants.FileFields.DB_PUBLISHED
import ebi.ac.uk.model.constants.FileFields.DB_SIZE
import java.io.File

/**
 * Source that allows the submitter to use store files directly in fire and bypass backend mechanism.
 */
internal object DbFilesSource : FilesSource {
    override val description: String
        get() = "Provided Db files"

    override suspend fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        val valuesMap = attributes.associateBy({ it.name }, { it.value })
        val dbFile = getDbFile(valuesMap)
        return if (dbFile != null) return asFireFile(path, dbFile, attributes) else null
    }

    override suspend fun getFileList(path: String): File? = null

    @Suppress("ComplexCondition")
    private fun getDbFile(attributes: Map<String, String?>): FireByPassFile? {
        val md5 = attributes[DB_MD5.value]
        val size = attributes[DB_SIZE.value]
        val id = attributes[DB_ID.value]
        val path = attributes[DB_PATH.value]
        val published = attributes[DB_PUBLISHED.value]

        return if (md5 == null || size == null || id == null || path == null) null
        else FireByPassFile(id, md5, path, size.toLong(), published.toBoolean())
    }

    private fun asFireFile(path: String, db: FireByPassFile, attributes: List<Attribute>): FireFile =
        FireFile(
            fireId = db.id,
            firePath = db.path,
            published = db.published,
            filePath = path,
            relPath = "Files/$path",
            md5 = db.md5,
            type = FILE,
            size = db.size,
            attributes = attributes.toExtAttributes(FILES_RESERVED_ATTRS)
        )

    private data class FireByPassFile(
        val id: String,
        val md5: String,
        val path: String,
        val size: Long,
        val published: Boolean,
    )
}
