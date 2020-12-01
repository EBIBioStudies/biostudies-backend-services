package ac.uk.ebi.biostd.persistence.doc.model

import arrow.core.Either
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

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

enum class DocSubmissionMethod { FILE, PAGE_TAB, UNKNOWN }
enum class DocProcessingStatus { PROCESSED, PROCESSING, REQUESTED }

data class DocTag(val name: String, val value: String)
data class DocProject(val accNo: String)
data class DocAttributeDetail(val name: String, val value: String)
data class DocLink(val url: String, val attributes: List<DocAttribute> = listOf())

data class DocFile(val filePath: String, val attributes: List<DocAttribute> = listOf(), val md5: String)
data class DocFileList(val fileName: String, val files: List<DocFile>)
data class DocSectionTable(val sections: List<DocSection>)
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

data class DocStat(val name: String, val value: String)
