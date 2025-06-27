package ac.uk.ebi.biostd.tsv.deserialization.model

import ac.uk.ebi.biostd.common.validatedFilePath
import ac.uk.ebi.biostd.tsv.deserialization.common.asTable
import ac.uk.ebi.biostd.tsv.deserialization.common.findId
import ac.uk.ebi.biostd.tsv.deserialization.common.getIdOrElse
import ac.uk.ebi.biostd.tsv.deserialization.common.getType
import ac.uk.ebi.biostd.tsv.deserialization.common.toAttributes
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_LINK_URL
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constants.FileFields
import ebi.ac.uk.util.collections.findSecond

sealed class TsvChunk(
    lines: List<TsvChunkLine>,
) {
    val header: TsvChunkLine = lines.first()
    val lines: List<TsvChunkLine> = lines.drop(1)

    val startIndex: Int
        get(): Int {
            return header.index
        }

    val endIndex: Int
        get(): Int {
            return lines.lastIndex
        }
}

internal class LinkChunk(
    body: List<TsvChunkLine>,
) : TsvChunk(body) {
    fun asLink(): Link {
        val linkUrl = getIdOrElse(InvalidElementException(REQUIRED_LINK_URL))
        val attributes = toAttributes(lines)
        return Link(linkUrl, attributes)
    }
}

class FileChunk(
    body: List<TsvChunkLine>,
) : TsvChunk(body) {
    fun asFile(): BioFile {
        val fileName = validatedFilePath(header.findSecond())
        val attributes = toAttributes(lines)
        val type = attributes.find { it.name == FileFields.TYPE.value }?.value
        return BioFile(fileName, attributes = attributes, type = type ?: FileFields.FILE_TYPE.value)
    }
}

internal class LinksTableChunk(
    body: List<TsvChunkLine>,
) : TsvChunk(body) {
    fun asTable() = LinksTable(asTable(this) { url, attributes -> Link(url, attributes) })
}

internal class FileTableChunk(
    body: List<TsvChunkLine>,
) : TsvChunk(body) {
    fun asTable() =
        FilesTable(
            asTable(this) { name, attributes ->
                BioFile(validatedFilePath(name), attributes = attributes)
            },
        )
}

internal sealed class SectionTableChunk(
    body: List<TsvChunkLine>,
) : TsvChunk(body) {
    open fun asTable() =
        SectionsTable(
            asTable(this) { accNo, attributes -> Section(this.getType(), accNo, attributes = attributes) },
        )
}

internal class RootSectionTableChunk(
    body: List<TsvChunkLine>,
) : SectionTableChunk(body)

internal class SubSectionTableChunk(
    body: List<TsvChunkLine>,
    val parent: String,
) : SectionTableChunk(body) {
    override fun asTable() =
        SectionsTable(
            asTable(this) { accNo, attributes ->
                Section(this.getType(), accNo, attributes = attributes, parentAccNo = parent)
            },
        )
}

internal sealed class SectionChunk(
    body: List<TsvChunkLine>,
) : TsvChunk(body) {
    fun asSection() = Section(type = getType(), accNo = findId(), attributes = toAttributes(lines))
}

internal class RootSubSectionChunk(
    body: List<TsvChunkLine>,
) : SectionChunk(body)

internal class SubSectionChunk(
    body: List<TsvChunkLine>,
    val parent: String,
) : SectionChunk(body)
