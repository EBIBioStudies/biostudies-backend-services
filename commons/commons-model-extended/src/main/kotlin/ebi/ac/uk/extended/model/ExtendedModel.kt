package ebi.ac.uk.extended.model

import arrow.core.Either
import com.fasterxml.jackson.databind.annotation.JsonAppend
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import ebi.ac.uk.extended.serialization.EitherListSerializer
import ebi.ac.uk.extended.serialization.ExtFileDeserializer
import ebi.ac.uk.extended.serialization.ExtFileSerializer
import ebi.ac.uk.extended.serialization.ExtElementTypeWriter
import java.io.File
import java.time.OffsetDateTime

enum class ExtSubmissionMethod { FILE, PAGE_TAB, UNKNOWN }

enum class ExtProcessingStatus { PROCESSED, PROCESSING }

data class ExtTag(val name: String, val value: String)

data class ExtAccessTag(val name: String)

data class ExtAttributeDetail(val name: String, val value: String)

@JsonAppend(props = [JsonAppend.Prop(name = "type", value = ExtElementTypeWriter::class)])
data class ExtLink(val url: String, val attributes: List<ExtAttribute>)

@JsonAppend(props = [JsonAppend.Prop(name = "type", value = ExtElementTypeWriter::class)])
data class ExtFile(
    val fileName: String,

    @JsonSerialize(using = ExtFileSerializer::class)
    val file: File,

    val attributes: List<ExtAttribute>
)

@JsonAppend(props = [JsonAppend.Prop(name = "type", value = ExtElementTypeWriter::class)])
data class ExtFileList(val fileName: String, val files: List<ExtFile>)

@JsonAppend(props = [JsonAppend.Prop(name = "type", value = ExtElementTypeWriter::class)])
data class ExtSectionTable(val sections: List<ExtSection>)

@JsonAppend(props = [JsonAppend.Prop(name = "type", value = ExtElementTypeWriter::class)])
data class ExtLinkTable(val links: List<ExtLink>)

@JsonAppend(props = [JsonAppend.Prop(name = "type", value = ExtElementTypeWriter::class)])
data class ExtFileTable(val files: List<ExtFile>) {
    constructor(vararg files: ExtFile) : this(files.toList())
}

data class ExtAttribute(
    val name: String,
    val value: String,
    val reference: Boolean,
    val nameAttrs: List<ExtAttributeDetail>,
    val valueAttrs: List<ExtAttributeDetail>
)

@JsonAppend(props = [JsonAppend.Prop(name = "type", value = ExtElementTypeWriter::class)])
data class ExtSection(
    val accNo: String? = null,

    val type: String,

    val fileList: ExtFileList? = null,

    val attributes: List<ExtAttribute> = listOf(),

    @JsonSerialize(using = EitherListSerializer::class)
    val sections: List<Either<ExtSection, ExtSectionTable>> = listOf(),

    @JsonSerialize(using = EitherListSerializer::class)
    val files: List<Either<ExtFile, ExtFileTable>> = listOf(),

    @JsonSerialize(using = EitherListSerializer::class)
    val links: List<Either<ExtLink, ExtLinkTable>> = listOf()
)

data class ExtSubmission(
    val accNo: String,
    var version: Int,
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
    val attributes: List<ExtAttribute>,
    val tags: List<ExtTag>,
    val accessTags: List<ExtAccessTag>,
    val section: ExtSection
)
