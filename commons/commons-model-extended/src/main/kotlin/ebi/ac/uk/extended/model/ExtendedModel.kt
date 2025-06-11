package ebi.ac.uk.extended.model

import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import java.io.File
import java.time.OffsetDateTime

enum class ExtSubmissionMethod { FILE, PAGE_TAB, UNKNOWN }

enum class ExtFileType(
    val value: String,
) {
    FILE("file"),
    DIR("directory"),
    ;

    companion object {
        fun fromString(value: String): ExtFileType =
            when (value) {
                FILE.value -> FILE
                DIR.value -> DIR
                else -> throw IllegalArgumentException("Unknown ExtFileType '$value'")
            }
    }
}

data class ExtTag(
    val name: String,
    val value: String,
)

data class ExtCollection(
    val accNo: String,
)

data class ExtAttributeDetail(
    val name: String,
    val value: String?,
)

data class ExtLink(
    val url: String,
    val attributes: List<ExtAttribute> = listOf(),
)

sealed interface ExtFile {
    val filePath: String
    val attributes: List<ExtAttribute>

    val fileName: String
        get() = filePath.substringAfterLast("/")
}

sealed interface PersistedExtFile : ExtFile {
    val relPath: String
    val md5: String
    val size: Long
    val type: ExtFileType
}

data class RequestFile(
    override val filePath: String,
    override val attributes: List<ExtAttribute>,
    val type: String,
) : ExtFile

data class FireFile(
    val fireId: String,
    val firePath: String,
    val published: Boolean,
    override val filePath: String,
    override val relPath: String,
    override val md5: String,
    override val size: Long,
    override val type: ExtFileType,
    override val attributes: List<ExtAttribute>,
) : PersistedExtFile

data class NfsFile(
    override val filePath: String,
    override val relPath: String,
    val file: File,
    val fullPath: String,
    val md5Calculated: Boolean,
    override val md5: String,
    override val size: Long,
    override val attributes: List<ExtAttribute> = listOf(),
    override val type: ExtFileType = if (file.isDirectory) DIR else FILE,
) : PersistedExtFile

data class ExtFileList(
    val filePath: String,
    val file: File,
    val filesUrl: String? = null,
    val pageTabFiles: List<ExtFile> = listOf(),
) {
    val fileName: String
        get() = filePath.substringAfterLast("/")
}

data class ExtLinkList(
    val filePath: String,
    val file: File,
    val links: List<ExtLink> = listOf(),
    val pageTabFiles: List<ExtFile> = listOf(),
) {
    val fileName: String
        get() = filePath.substringAfterLast("/")
}

data class ExtSectionTable(
    val sections: List<ExtSection>,
)

data class ExtLinkTable(
    val links: List<ExtLink>,
)

data class ExtFileTable(
    val files: List<ExtFile>,
) {
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
    val linkList: ExtLinkList? = null,
    val attributes: List<ExtAttribute> = listOf(),
    val sections: List<Either<ExtSection, ExtSectionTable>> = listOf(),
    val files: List<Either<ExtFile, ExtFileTable>> = listOf(),
    val links: List<Either<ExtLink, ExtLinkTable>> = listOf(),
)

data class ExtAccessTag(
    val name: String,
)

interface ExtSubmissionInfo {
    val accNo: String
    val version: Int
    val owner: String
    val released: Boolean
    val secretKey: String
    val relPath: String
    val storageMode: StorageMode
}

data class ExtSubmission(
    override val accNo: String,
    override var version: Int,
    override val owner: String,
    var schemaVersion: String,
    val submitter: String,
    val title: String?,
    val doi: String?,
    val method: ExtSubmissionMethod,
    override val relPath: String,
    val rootPath: String?,
    override val released: Boolean,
    override val secretKey: String,
    val releaseTime: OffsetDateTime?,
    val modificationTime: OffsetDateTime,
    val creationTime: OffsetDateTime,
    val section: ExtSection,
    val attributes: List<ExtAttribute> = listOf(),
    val tags: List<ExtTag> = listOf(),
    val collections: List<ExtCollection> = listOf(),
    val pageTabFiles: List<ExtFile> = listOf(),
    override val storageMode: StorageMode,
) : ExtSubmissionInfo

enum class StorageMode(
    val value: String,
) {
    FIRE("FIRE"),
    NFS("NFS"),
    ;

    companion object {
        fun fromString(value: String): StorageMode =
            when (value) {
                "FIRE" -> FIRE
                "NFS" -> NFS
                else -> error("Unknown storage mode $value")
            }
    }
}

data class ExtUser(
    val email: String,
    val fullName: String,
    val login: String?,
    val notificationsEnabled: Boolean,
)
