package ebi.ac.uk.extended.model

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY
import ebi.ac.uk.extended.delegates.AccessTagDelegate
import java.io.File
import java.time.OffsetDateTime

enum class ExtSubmissionMethod { FILE, PAGE_TAB, UNKNOWN }

enum class ExtProcessingStatus { PROCESSED, PROCESSING, REQUESTED }

data class ExtTag(val name: String, val value: String)

data class Project(val accNo: String)

data class ExtAttributeDetail(val name: String, val value: String)

data class ExtLink(
    val url: String,
    val attributes: List<ExtAttribute> = listOf()
)

data class ExtFile(
    val fileName: String,
    val file: File,
    val attributes: List<ExtAttribute> = listOf()
)

data class ExtFileList(val fileName: String, val files: List<ExtFile>)

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
    val projects: List<Project> = listOf(),
    val stats: List<ExtStat> = listOf()
) {
    @get:JsonProperty(access = READ_ONLY)
    val accessTags: List<ExtAccessTag> by AccessTagDelegate()
}

data class ExtStat(val name: String, val value: String)

data class ExtUser(
    val email: String,
    val fullName: String,
    val login: String?,
    val notificationsEnabled: Boolean
)
