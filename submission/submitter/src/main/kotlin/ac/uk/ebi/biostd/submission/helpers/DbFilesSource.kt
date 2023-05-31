package ac.uk.ebi.biostd.submission.helpers

import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS
import ebi.ac.uk.model.constants.FileFields
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

    override fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        val valuesMap = attributes.associateBy({ it.name }, { it.value })
        val dbFile = getDbFile(valuesMap)
        return if (dbFile != null) return asFireFile(path, dbFile, attributes) else null
    }

    override fun getFileList(path: String): File? = null

    @Suppress("ComplexCondition")
    private fun getDbFile(attributes: Map<String, String?>): FireByPassFile? {
        fun requireNullOrNotEmpty(field: FileFields): String? {
            val value = attributes[field.value]
            require(value == null || value.isNotBlank()) { "${field.value} can not be an empty string" }
            return value
        }

        fun checkPath(): String? {
            val path = requireNullOrNotEmpty(DB_PATH)
            if (path?.startsWith("/").orFalse()) throw IllegalArgumentException("Db path '$path' needs to be relative.")
            return path
        }

        val md5 = requireNullOrNotEmpty(DB_MD5)
        val size = requireNullOrNotEmpty(DB_SIZE)
        val id = requireNullOrNotEmpty(DB_ID)
        val path = checkPath()
        val published = requireNullOrNotEmpty(DB_PUBLISHED)
        val values = listOf(md5, size, id, path, published)
        return when {
            values.all { it == null } -> null
            values.all { it != null } -> FireByPassFile(id!!, md5!!, path!!, size!!.toLong(), published.toBoolean())
            else -> throw IllegalArgumentException(
                "All bypass attributes [md5, size, id, path, published] need to be present or none, found $values"
            )
        }
    }

    private fun asFireFile(path: String, db: FireByPassFile, attributes: List<Attribute>): FireFile =
        FireFile(
            fireId = db.id,
            firePath = db.path,
            published = db.published,
            filePath = path,
            relPath = "Files/$path",
            md5 = db.md5,
            type = ExtFileType.FILE,
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
