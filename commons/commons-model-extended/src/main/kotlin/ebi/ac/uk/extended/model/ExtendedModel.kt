package ebi.ac.uk.extended.model

import arrow.core.Either
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import java.io.File
import java.time.OffsetDateTime

enum class ExtSubmissionMethod { FILE, PAGE_TAB, UNKNOWN }

enum class ExtProcessingStatus { PROCESSED, PROCESSING, REQUESTED }

data class ExtTag(val name: String, val value: String)

data class ExtCollection(val accNo: String)

data class ExtAttributeDetail(val name: String, val value: String)

data class ExtLink(
    val url: String,
    val attributes: List<ExtAttribute> = listOf()
)

sealed class ExtFile {
    abstract val filePath: String
    abstract val relPath: String
    abstract val attributes: List<ExtAttribute>
    val fileName: String
        get() = filePath.substringAfterLast("/")
}

data class FireFile(
    override val filePath: String,
    override val relPath: String,
    val fireId: String,
    val md5: String,
    val size: Long,
    override val attributes: List<ExtAttribute>
) : ExtFile()

data class FireDirectory(
    override val filePath: String,
    override val relPath: String,
    val md5: String,
    val size: Long,
    override val attributes: List<ExtAttribute>
) : ExtFile()

data class NfsFile(
    override val filePath: String,
    override val relPath: String,
    val fullPath: String,
    val file: File,
    override val attributes: List<ExtAttribute> = listOf()
) : ExtFile() {

    constructor(filePath: String, relPath: String, file: File, attributes: List<ExtAttribute> = listOf()) :
        this(filePath, relPath, file.absolutePath, file, attributes)

    // TODO Once SQL is removed, this field should be removed and md5 should be set as a constructor property
    private var _md5: String = ""

    var md5: String
        get(): String {
            if (_md5.isBlank()) _md5 = file.md5()
            return _md5
        }
        set(value) {
            _md5 = value
        }

    val size: Long
        get() = file.size()
}

data class ExtFileList(
    val fileName: String,
    val files: List<ExtFile> = listOf(),
    val filesUrl: String? = null,
    val pageTabFiles: List<ExtFile> = listOf()
)

data class ExtSectionTable(val sections: List<ExtSection>)

data class ExtLinkTable(val links: List<ExtLink>)

data class ExtFileTable(val files: List<ExtFile>) {
    constructor(vararg files: ExtFile) : this(files.toList())
}

data class ExtAttribute(
    val name: String,
    val value: String,
    val reference: Boolean = false,
    val nameAttrs: List<ExtAttributeDetail> = listOf(),
    val valueAttrs: List<ExtAttributeDetail> = listOf()
)

data class ExtSection(
    val accNo: String? = null,
    val type: String,
    val fileList: ExtFileList? = null,
    val attributes: List<ExtAttribute> = listOf(),
    val sections: List<Either<ExtSection, ExtSectionTable>> = listOf(),
    val files: List<Either<ExtFile, ExtFileTable>> = listOf(),
    val links: List<Either<ExtLink, ExtLinkTable>> = listOf()
)

data class ExtAccessTag(val name: String)

data class ExtSubmission(
    val accNo: String,
    var version: Int,
    var schemaVersion: String = "1.0",
    val owner: String,
    val submitter: String,
    val title: String?,
    val method: ExtSubmissionMethod,
    val relPath: String,
    val rootPath: String?,
    val released: Boolean,
    val secretKey: String,
    val status: ExtProcessingStatus,
    val releaseTime: OffsetDateTime?,
    val modificationTime: OffsetDateTime,
    val creationTime: OffsetDateTime,
    val section: ExtSection,
    val attributes: List<ExtAttribute> = listOf(),
    val tags: List<ExtTag> = listOf(),
    val collections: List<ExtCollection> = listOf(),
    val stats: List<ExtStat> = listOf(),
    val pageTabFiles: List<ExtFile> = listOf()
)

// TODO change value type to long
data class ExtStat(val name: String, val value: String)

data class ExtUser(
    val email: String,
    val fullName: String,
    val login: String?,
    val notificationsEnabled: Boolean
)
