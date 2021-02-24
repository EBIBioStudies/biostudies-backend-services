package ac.uk.ebi.biostd.persistence.doc.model

import arrow.core.Either
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
    val id: String?,
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
    val projects: List<DocProject> = listOf(),
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
data class DocProject(val accNo: String)
data class DocAttributeDetail(val name: String, val value: String)
data class DocLink(val url: String, val attributes: List<DocAttribute> = listOf())

data class DocFile(
    val relPath: String,
    val fullPath: String,
    val attributes: List<DocAttribute> = listOf(),
    val md5: String
)

data class DocFileList(val fileName: String, val files: List<DocFile>)
data class DocSectionTable(val sections: List<DocTableSection>)
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
    val accNo: String? = null,
    val type: String,
    val fileList: DocFileList? = null,
    val attributes: List<DocAttribute> = listOf(),
    val sections: List<Either<DocSection, DocSectionTable>> = listOf(),
    val files: List<Either<DocFile, DocFileTable>> = listOf(),
    val links: List<Either<DocLink, DocLinkTable>> = listOf()
)

data class DocTableSection(
    val accNo: String? = null,
    val type: String,
    val attributes: List<DocAttribute> = listOf()
)

data class DocStat(val name: String, val value: Long)
