package uk.ac.ebi.io.sources

import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.FILE
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
internal object DbFilesSource : FilesSource {
    override val description: String
        get() = "Provided Db files"

    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<Attribute>,
    ): ExtFile? {
        val valuesMap = attributes.associateBy({ it.name }, { it.value })
        val dbFile = getDbFile(valuesMap)
        return if (dbFile != null) return asFireFile(path, dbFile, attributes) else null
    }

    override suspend fun getFileList(path: String): File? = null

    @Suppress("ComplexCondition")
    private fun getDbFile(attributes: Map<String, String?>): ByPassFile? {
        fun requireNullOrNotEmpty(field: FileFields): String? {
            val value = attributes[field.value]
            require(value == null || value.isNotBlank()) { "${field.value} can not be an empty string" }
            return value
        }

        fun checkPath(): String? {
            val path = requireNullOrNotEmpty(DB_PATH)
            require(path == null || path.startsWith("/").not()) { "Db path '$path' needs to be relative." }
            return path
        }

        val md5 = requireNullOrNotEmpty(DB_MD5)
        val size = requireNullOrNotEmpty(DB_SIZE)?.toLong()
        val id = requireNullOrNotEmpty(DB_ID)
        val path = checkPath()
        val public = requireNullOrNotEmpty(DB_PUBLISHED)?.toBoolean()

        if (size != null && id != null && path != null && md5 != null && public != null) {
            return ByPassFile(id, md5, path, size, public)
        }

        if (size == null && id == null && path == null && public == null) {
            return null
        }

        throw IllegalArgumentException(
            "All bypass attributes [md5, size, id, path, published] need to be present or none, " +
                "found [$md5, $size, $id, $path, $public]",
        )
    }

    private fun asFireFile(
        path: String,
        db: ByPassFile,
        attributes: List<Attribute>,
    ): FireFile =
        FireFile(
            fireId = db.id,
            firePath = db.path,
            published = db.published,
            filePath = path,
            relPath = "Files/$path",
            md5 = db.md5,
            type = FILE,
            size = db.size,
            attributes = attributes.toExtAttributes(FILES_RESERVED_ATTRS),
        )

    private data class ByPassFile(
        val id: String,
        val md5: String,
        val path: String,
        val size: Long,
        val published: Boolean,
    )
}
