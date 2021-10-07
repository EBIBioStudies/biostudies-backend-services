package ac.uk.ebi.biostd.persistence.doc.model

import arrow.core.Either
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

val nfsDocFileClass: String = NfsDocFile::class.java.canonicalName
val fireDocFileClass: String = FireDocFile::class.java.canonicalName
val docFileTableClass: String = DocFileTable::class.java.canonicalName
val docLinkClass: String = DocLink::class.java.canonicalName
val docLinkTableClass: String = DocLinkTable::class.java.canonicalName
val docSectionClass: String = DocSection::class.java.canonicalName
val docSectionTableClass: String = DocSectionTable::class.java.canonicalName
val docSubmissionClass: String = DocSubmission::class.java.canonicalName

@Document(collection = "submissions")
data class DocSubmission(
    @Id
    val id: ObjectId,
    val accNo: String,
    var version: Int,
    val owner: String,
    val submitter: String,
    val title: String?,
    val method: DocSubmissionMethod,
    val relPath: String,
    val rootPath: String?,
    val released: Boolean,
    val secretKey: String,
    val status: DocProcessingStatus,
    val releaseTime: Instant?,
    val modificationTime: Instant,
    val creationTime: Instant,
    val section: DocSection,
    val attributes: List<DocAttribute> = listOf(),
    val tags: List<DocTag> = listOf(),
    val collections: List<DocCollection> = listOf(),
    val stats: List<DocStat> = listOf()
)

enum class DocSubmissionMethod(val value: String) {
    FILE("FILE"), PAGE_TAB("PAGE_TAB"), UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String): DocSubmissionMethod {
            return when (value) {
                "FILE" -> FILE
                "PAGE_TAB" -> PAGE_TAB
                "UNKNOWN" -> UNKNOWN
                else -> throw IllegalStateException("Unknown submission method $value")
            }
        }
    }
}

enum class DocProcessingStatus(val value: String) {
    PROCESSED("PROCESSED"), PROCESSING("PROCESSING"), REQUESTED("REQUESTED");

    companion object {
        fun fromString(value: String): DocProcessingStatus {
            return when (value) {
                "PROCESSED" -> PROCESSED
                "PROCESSING" -> PROCESSING
                "REQUESTED" -> REQUESTED
                else -> throw IllegalStateException("Unknown submission method $value")
            }
        }
    }
}

data class DocTag(val name: String, val value: String)
data class DocCollection(val accNo: String)
data class DocAttributeDetail(val name: String, val value: String)
data class DocLink(val url: String, val attributes: List<DocAttribute> = listOf())

sealed class DocFile(
    open val fileName: String,
    open val filePath: String,
    open val relPath: String,
    open val attributes: List<DocAttribute>,
    open val md5: String,
    open val fileSize: Long
)

data class NfsDocFile(
    override val fileName: String,
    override val filePath: String,
    override val relPath: String,
    val fullPath: String,
    override var attributes: List<DocAttribute>,
    override val md5: String,
    override val fileSize: Long,
    val fileType: String,
) : DocFile(fileName, filePath, relPath, attributes, md5, fileSize)

data class FireDocFile(
    override val fileName: String,
    override val filePath: String,
    override val relPath: String,
    val fireId: String,
    override val attributes: List<DocAttribute>,
    override val md5: String,
    override val fileSize: Long,
) : DocFile(fileName, filePath, relPath, attributes, md5, fileSize)

data class FireDocDirectory(
    override val fileName: String,
    override val filePath: String,
    override val relPath: String,
    override val attributes: List<DocAttribute>,
    override val md5: String,
    override val fileSize: Long
) : DocFile(fileName, filePath, relPath, attributes, md5, fileSize)

data class DocFileList(
    val fileName: String,
    val files: List<DocFileRef>
)

data class DocFileRef(
    val fileId: ObjectId
)

@Document(collection = "file_list_files")
data class FileListDocFile(
    @Id
    val id: ObjectId,
    val submissionId: ObjectId,
    val fileName: String,
    val location: String,
    val attributes: List<DocAttribute> = listOf(),
    val md5: String,
    val size: Long,
    val fileSystem: FileSystem
)

enum class FileSystem {
    NFS, FIRE, FIRE_DIR
}

data class DocSectionTable(val sections: List<DocSectionTableRow>)
data class DocLinkTable(val links: List<DocLink>)

data class DocFileTable(val files: List<DocFile>)

data class DocAttribute(
    val name: String,
    val value: String,
    val reference: Boolean = false,
    val nameAttrs: List<DocAttributeDetail> = listOf(),
    val valueAttrs: List<DocAttributeDetail> = listOf()
)

data class DocSection(
    val id: ObjectId,
    val accNo: String? = null,
    val type: String,
    val fileList: DocFileList? = null,
    val attributes: List<DocAttribute> = listOf(),
    val sections: List<Either<DocSection, DocSectionTable>> = listOf(),
    val files: List<Either<DocFile, DocFileTable>> = listOf(),
    val links: List<Either<DocLink, DocLinkTable>> = listOf()
)

data class DocSectionTableRow(
    val accNo: String? = null,
    val type: String,
    val attributes: List<DocAttribute> = listOf()
)

data class DocStat(val name: String, val value: Long)
