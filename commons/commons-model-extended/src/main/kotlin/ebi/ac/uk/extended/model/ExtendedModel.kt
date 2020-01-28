package ebi.ac.uk.extended.model

import arrow.core.Either
import ebi.ac.uk.model.constants.ProcessingStatus
import java.io.File
import java.time.OffsetDateTime

data class ExtAttributeDetail(val name: String, val value: String)
data class ExtLink(val url: String, val attributes: List<ExtAttribute>)

data class ExtFile(val fileName: String, val file: File, val attributes: List<ExtAttribute>)
data class ExtFileList(val fileName: String, val file: File, val files: List<ExtFile>)

data class ExtSectionTable(val sections: List<ExtSection>)
data class ExtFileTable(val files: List<ExtFile>) {
    constructor(vararg files: ExtFile) : this(files.toList())
}

data class ExtLinkTable(val links: List<ExtLink>)

data class ExtAttribute(
    val name: String,
    val value: String,
    val reference: Boolean,
    val nameAttrs: List<ExtAttributeDetail>,
    val valueAttrs: List<ExtAttributeDetail>
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

data class ExtSubmission(
    val accNo: String,
    val title: String?,
    val relPath: String,
    val rootPath: String?,
    val released: Boolean,
    val secretKey: String,
    val status: ProcessingStatus,
    val releaseTime: OffsetDateTime?,
    val modificationTime: OffsetDateTime,
    val creationTime: OffsetDateTime,
    val attributes: List<ExtAttribute>,
    val tags: List<ExtTag>,
    val accessTags: List<ExtAccessTag>,
    val section: ExtSection
)

data class ExtTag(val name: String, val value: String)
data class ExtAccessTag(val name: String)
