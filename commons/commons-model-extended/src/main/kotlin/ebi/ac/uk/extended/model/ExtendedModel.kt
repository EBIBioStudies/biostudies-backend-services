package ebi.ac.uk.extended.model

import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import java.io.File
import java.time.OffsetDateTime

enum class ExtSubmissionMethod { FILE, PAGE_TAB, UNKNOWN }

enum class ExtProcessingStatus { PROCESSED, PROCESSING, REQUESTED }

enum class ExtFileType(val value: String) {
    FILE("file"),
    DIR("directory");

    companion object {
        fun fromString(value: String): ExtFileType = when (value) {
            FILE.value -> FILE
            DIR.value -> DIR
            else -> throw IllegalArgumentException("Unknown ExtFileType '$value'")
        }
    }
}

data class ExtTag(val name: String, val value: String)

data class ExtCollection(val accNo: String)

data class ExtAttributeDetail(val name: String, val value: String?)

data class ExtLink(
    val url: String,
    val attributes: List<ExtAttribute> = listOf(),
)

sealed class ExtFile {
    abstract val filePath: String
    abstract val relPath: String
    abstract val attributes: List<ExtAttribute>
    abstract val md5: String
    abstract val type: ExtFileType
    abstract val size: Long

    val fileName: String
        get() = filePath.substringAfterLast("/")
}

data class FireFile(
    override val filePath: String,
    override val relPath: String,
    val fireId: String,
    override val md5: String,
    override val size: Long,
    override val type: ExtFileType,
    override val attributes: List<ExtAttribute>,
) : ExtFile()

data class NfsFile(
    override val filePath: String,
    override val relPath: String,
    val file: File,
    val fullPath: String,
    override val md5: String,
    override val size: Long,
    override val attributes: List<ExtAttribute> = listOf(),
) : ExtFile() {
    override val type: ExtFileType
        get() = if (file.isDirectory) DIR else FILE
}

data class ExtFileList(
    val filePath: String,
    val file: File,
    val filesUrl: String? = null,
    val pageTabFiles: List<ExtFile> = listOf(),
) {
    val fileName: String
        get() = filePath.substringAfterLast("/")
}

data class ExtSectionTable(val sections: List<ExtSection>)

data class ExtLinkTable(val links: List<ExtLink>)

data class ExtFileTable(val files: List<ExtFile>) {
    constructor(vararg files: ExtFile) : this(files.toList())
}

data class ExtAttribute(
    val name: String,
    val value: String?,
    val reference: Boolean = false,
    val nameAttrs: List<ExtAttributeDetail> = listOf(),
    val valueAttrs: List<ExtAttributeDetail> = listOf(),
)

data class ExtSection(
    val accNo: String? = null,
    val type: String,
    val fileList: ExtFileList? = null,
    val attributes: List<ExtAttribute> = listOf(),
    val sections: List<Either<ExtSection, ExtSectionTable>> = listOf(),
    val files: List<Either<ExtFile, ExtFileTable>> = listOf(),
    val links: List<Either<ExtLink, ExtLinkTable>> = listOf(),
)

data class ExtAccessTag(val name: String)

data class ExtSubmission(
    val accNo: String,
    var version: Int,
    var schemaVersion: String,
    val owner: String,
    val submitter: String,
    val title: String?,
    val method: ExtSubmissionMethod,
    val relPath: String,
    val rootPath: String?,
    val released: Boolean,
    val secretKey: String,
    val releaseTime: OffsetDateTime?,
    val modificationTime: OffsetDateTime,
    val creationTime: OffsetDateTime,
    val section: ExtSection,
    val attributes: List<ExtAttribute> = listOf(),
    val tags: List<ExtTag> = listOf(),
    val collections: List<ExtCollection> = listOf(),
    val stats: List<ExtStat> = listOf(),
    val pageTabFiles: List<ExtFile> = listOf(),
    val storageMode: StorageMode,
)

enum class StorageMode(val value: String) {
    FIRE("FIRE"), NFS("NFS");

    companion object {
        fun fromString(value: String): StorageMode {
            return when (value) {
                "FIRE" -> FIRE
                "NFS" -> NFS
                else -> throw IllegalStateException("Unknown storage mode $value")
            }
        }
    }
}

// TODO change value type to long
data class ExtStat(val name: String, val value: String)

data class ExtUser(
    val email: String,
    val fullName: String,
    val login: String?,
    val notificationsEnabled: Boolean,
)
