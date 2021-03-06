package ac.uk.ebi.biostd.persistence.doc.model

import arrow.core.Either
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

val docAttributeDetailClass: String = DocAttributeDetail::class.java.canonicalName
val docFileClass: String = DocFile::class.java.canonicalName
val docFileListClass: String = DocFileList::class.java.canonicalName
val docFileTableClass: String = DocFileTable::class.java.canonicalName
val docLinkClass: String = DocLink::class.java.canonicalName
val docLinkTableClass: String = DocLinkTable::class.java.canonicalName
val docSectionClass: String = DocSection::class.java.canonicalName
val docSectionTableClass: String = DocSectionTable::class.java.canonicalName
val docSubmissionClass: String = DocSubmission::class.java.canonicalName
val docSubmissionMethodClass: String = DocSubmissionMethod::class.java.canonicalName

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

// TODO fullPath should be changed to "location" since it's more generic
// TODO fileSystem is not being persisted in the database
data class DocFile(
    val relPath: String,
    val fullPath: String,
    val attributes: List<DocAttribute> = listOf(),
    val md5: String,
    val fileType: String,
    val fileSize: Long,
    val fileSystem: FileSystem
)

data class DocFileList(
    val fileName: String,
    val files: List<DocFileRef>
)

data class DocFileRef(
    val fileId: ObjectId
)

// TODO fullPath should be changed to "location" since it's more generic
// TODO fileSystem is not being persisted in the database
@Document(collection = "file_list_files")
data class FileListDocFile(
    @Id
    val id: ObjectId,
    val submissionId: ObjectId,
    val fileName: String,
    val fullPath: String,
    val attributes: List<DocAttribute> = listOf(),
    val md5: String,
    val fileSystem: FileSystem
)

enum class FileSystem {
    NFS, FIRE
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
