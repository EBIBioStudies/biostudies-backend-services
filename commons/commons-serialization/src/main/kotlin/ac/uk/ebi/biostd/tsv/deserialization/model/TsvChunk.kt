package ac.uk.ebi.biostd.tsv.deserialization.model

import ac.uk.ebi.biostd.tsv.deserialization.common.asTable
import ac.uk.ebi.biostd.tsv.deserialization.common.findId
import ac.uk.ebi.biostd.tsv.deserialization.common.getIdOrElse
import ac.uk.ebi.biostd.tsv.deserialization.common.getType
import ac.uk.ebi.biostd.tsv.deserialization.common.toAttributes
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ac.uk.ebi.biostd.validation.REQUIRED_LINK_URL
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable

sealed class TsvChunk(lines: List<TsvChunkLine>) {

    val header = lines.first()
    val lines = lines.drop(1)

    val startIndex: Int
        get(): Int {
            return header.index
        }

    val endIndex: Int
        get(): Int {
            return lines.lastIndex
        }
}

class LinkChunk(body: List<TsvChunkLine>) : TsvChunk(body) {

    fun asLink(): Link {
        val linkUrl = getIdOrElse(InvalidElementException(REQUIRED_LINK_URL))
        val attributes = toAttributes(lines)
        return Link(linkUrl, attributes)
    }
}

class FileChunk(body: List<TsvChunkLine>) : TsvChunk(body) {

    fun asFile(): File {
        val fileName = getIdOrElse(InvalidElementException(REQUIRED_FILE_PATH))
        val attributes = toAttributes(lines)
        return File(fileName, attributes = attributes)
    }
}

class LinksTableChunk(body: List<TsvChunkLine>) : TsvChunk(body) {

    fun asTable() = LinksTable(asTable(this) { url, attributes -> Link(url, attributes) })
}

class FileTableChunk(body: List<TsvChunkLine>) : TsvChunk(body) {

    fun asTable() = FilesTable(asTable(this) { name, attributes -> File(name, attributes = attributes) })
}

sealed class SectionTableChunk(body: List<TsvChunkLine>) : TsvChunk(body) {

    fun asTable() = SectionsTable(
        asTable(this) { accNo, attributes -> Section(this.getType(), accNo, attributes = attributes) })
}

class RootSectionTableChunk(body: List<TsvChunkLine>) : SectionTableChunk(body)
class SubSectionTableChunk(body: List<TsvChunkLine>, val parent: String) : SectionTableChunk(body)

sealed class SectionChunk(body: List<TsvChunkLine>) : TsvChunk(body) {

    fun asSection() = Section(type = getType(), accNo = findId(), attributes = toAttributes(lines))
}

class RootSubSectionChunk(body: List<TsvChunkLine>) : SectionChunk(body)
class SubSectionChunk(body: List<TsvChunkLine>, val parent: String) : SectionChunk(body)
