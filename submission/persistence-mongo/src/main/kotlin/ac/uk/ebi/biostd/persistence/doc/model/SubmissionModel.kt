package ac.uk.ebi.biostd.persistence.doc.model

import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.StorageMode
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

val nfsDocFileClass: String = NfsDocFile::class.java.canonicalName
val fireDocFileClass: String = FireDocFile::class.java.canonicalName
val fileListDocFileDocFileClass: String = FileListDocFile::class.java.canonicalName
val linkListDocLinkDocFileClass: String = LinkListDocLink::class.java.canonicalName
val docSubmissionClass: String = DocSubmission::class.java.canonicalName

@Document(collection = "submissions")
data class DocSubmission(
    @Id
    val id: ObjectId,
    val accNo: String,
    var version: Int,
    var schemaVersion: String,
    val owner: String,
    val submitter: String,
    val title: String?,
    val doi: String?,
    val method: DocSubmissionMethod,
    val relPath: String,
    val rootPath: String?,
    val released: Boolean,
    val secretKey: String,
    val releaseTime: Instant?,
    val submissionTime: Instant,
    val modificationTime: Instant,
    val creationTime: Instant,
    val section: DocSection,
    val attributes: List<DocAttribute> = listOf(),
    val tags: List<DocTag> = listOf(),
    val collections: List<DocCollection> = listOf(),
    val pageTabFiles: List<DocFile> = listOf(),
    val storageMode: StorageMode,
)

enum class DocSubmissionMethod(
    val value: String,
) {
    FILE("FILE"),
    PAGE_TAB("PAGE_TAB"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromString(value: String): DocSubmissionMethod =
            when (value) {
                "FILE" -> FILE
                "PAGE_TAB" -> PAGE_TAB
                "UNKNOWN" -> UNKNOWN
                else -> error("Unknown submission method $value")
            }
    }
}

data class DocTag(
    val name: String,
    val value: String,
)

data class DocCollection(
    val accNo: String,
)

data class DocAttributeDetail(
    val name: String,
    val value: String?,
)

data class DocLink(
    val url: String,
    val attributes: List<DocAttribute> = listOf(),
)

@Suppress("LongParameterList")
sealed interface DocFile {
    val filePath: String
    val attributes: List<DocAttribute>
}

data class RequestDocFile(
    override val filePath: String,
    override val attributes: List<DocAttribute>,
    val type: String,
) : DocFile

data class NfsDocFile(
    val fileName: String,
    override val filePath: String,
    val relPath: String,
    val fullPath: String,
    override var attributes: List<DocAttribute>,
    val md5: String,
    val fileSize: Long,
    val fileType: String,
) : DocFile

data class FireDocFile(
    val fileName: String,
    override val filePath: String,
    val relPath: String,
    val fireId: String,
    override val attributes: List<DocAttribute>,
    val md5: String,
    val fileSize: Long,
    val fileType: String,
) : DocFile

data class DocFileList(
    val fileName: String,
    val pageTabFiles: List<DocFile> = listOf(),
)

data class DocLinkList(
    val fileName: String,
    val pageTabFiles: List<DocFile> = listOf(),
)

@Document(collection = "file_list_files")
data class FileListDocFile(
    @Id
    val id: ObjectId,
    val submissionId: ObjectId,
    val file: DocFile,
    val fileListName: String,
    val index: Int,
    val submissionVersion: Int,
    val submissionAccNo: String,
)

@Document(collection = "link_list_links")
data class LinkListDocLink(
    @Id
    val id: ObjectId,
    val submissionId: ObjectId,
    val link: DocLink,
    val linkListName: String,
    val index: Int,
    val submissionVersion: Int,
    val submissionAccNo: String,
)

data class DocSectionTable(
    val sections: List<DocSectionTableRow>,
)

data class DocLinkTable(
    val links: List<DocLink>,
)

data class DocFileTable(
    val files: List<DocFile>,
)

data class DocAttribute(
    val name: String,
    val value: String?,
    val reference: Boolean = false,
    val nameAttrs: List<DocAttributeDetail> = listOf(),
    val valueAttrs: List<DocAttributeDetail> = listOf(),
)

data class DocSection(
    val id: ObjectId,
    val accNo: String? = null,
    val type: String,
    val linkList: DocLinkList? = null,
    val fileList: DocFileList? = null,
    val attributes: List<DocAttribute> = listOf(),
    val sections: List<Either<DocSection, DocSectionTable>> = listOf(),
    val files: List<Either<DocFile, DocFileTable>> = listOf(),
    val links: List<Either<DocLink, DocLinkTable>> = listOf(),
)

data class DocSectionTableRow(
    val accNo: String? = null,
    val type: String,
    val attributes: List<DocAttribute> = listOf(),
)

@Document(collection = "submission_stats")
data class DocSubmissionStats(
    @Id
    val id: ObjectId,
    val accNo: String,
    val stats: Map<String, Long>,
    val lastUpdated: Instant? = null,
)
