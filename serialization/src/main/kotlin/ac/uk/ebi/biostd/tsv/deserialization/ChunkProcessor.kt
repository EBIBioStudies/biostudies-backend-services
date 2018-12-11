package ac.uk.ebi.biostd.tsv.deserialization

import ac.uk.ebi.biostd.tsv.deserialization.ext.findIdentifier
import ac.uk.ebi.biostd.tsv.deserialization.ext.getIdentifier
import ac.uk.ebi.biostd.tsv.deserialization.ext.getType
import ac.uk.ebi.biostd.tsv.deserialization.ext.isNameDetail
import ac.uk.ebi.biostd.tsv.deserialization.ext.isReference
import ac.uk.ebi.biostd.tsv.deserialization.ext.isValueDetail
import ac.uk.ebi.biostd.tsv.deserialization.ext.name
import ac.uk.ebi.biostd.tsv.deserialization.model.FileChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.FileTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinkChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinksTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSubSectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SectionContext
import ac.uk.ebi.biostd.tsv.deserialization.model.SectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SubSectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SubSectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields

private const val ALLOWED_TYPES = "Study"

class ChunkProcessor {

    fun getSubmission(tsvChunk: TsvChunk): Submission {
        requireType(SubFields.SUBMISSION.value, tsvChunk)
        return Submission(
            accNo = tsvChunk.findIdentifier().orEmpty(),
            attributes = toAttributes(tsvChunk.lines)
        )
    }

    fun getRootSection(tsvChunk: TsvChunk): Section {
        requireType(ALLOWED_TYPES, tsvChunk)

        try {
            return Section(
                accNo = tsvChunk.findIdentifier(),
                type = tsvChunk.getType(),
                attributes = toAttributes(tsvChunk.lines))
        } catch (exception: Exception) {
            throw exception
        }
    }

    fun processChunk(chunk: TsvChunk, sectionContext: SectionContext) {
        when (chunk) {
            is LinkChunk ->
                sectionContext.currentSection.addLink(Link(chunk.getIdentifier(), toAttributes(chunk.lines)))
            is FileChunk ->
                sectionContext.currentSection.addFile(File(chunk.getIdentifier(), toAttributes(chunk.lines)))
            is LinksTableChunk ->
                sectionContext.currentSection.addLinksTable(LinksTable(asTable(chunk) { url, attributes -> Link(url, attributes) }))
            is FileTableChunk ->
                sectionContext.currentSection.addFilesTable(FilesTable(asTable(chunk) { path, attributes -> File(path, attributes) }))
            is SectionTableChunk -> {
                val table = SectionsTable(asTable(chunk) { accNo, attributes -> Section(chunk.getType(), accNo, attributes = attributes) })
                when (chunk) {
                    is RootSectionTableChunk -> sectionContext.rootSection.addSectionTable(table)
                    is SubSectionTableChunk -> sectionContext.getValue(chunk.parent).addSectionTable(table)
                }
            }
            is SectionChunk -> {
                val newSection = createSingleSection(chunk)
                sectionContext.update(newSection)
                when (chunk) {
                    is RootSubSectionChunk -> sectionContext.rootSection.addSection(newSection)
                    is SubSectionChunk -> sectionContext.getValue(chunk.parent).addSection(newSection)
                }
            }
        }
    }

    private fun createSingleSection(chunk: TsvChunk) =
        Section(type = chunk.getType(), accNo = chunk.findIdentifier(), attributes = toAttributes(chunk.lines))

    private fun requireType(type: String, tsvChunk: TsvChunk) =
        require(tsvChunk.getType().equals(type, ignoreCase = true)) { "Expected to find block type of $type in block $tsvChunk" }

    private fun toAttributes(chunkLines: List<TsvChunkLine>): MutableList<Attribute> {
        val attributes: MutableList<Attribute> = mutableListOf()
        chunkLines.forEach { line ->
            when {
                line.isNameDetail() -> attributes.last().nameAttrs.add(AttributeDetail(line.name(), line.value))
                line.isValueDetail() -> attributes.last().valueAttrs.add(AttributeDetail(line.name(), line.value))
                else -> attributes.add(Attribute(line.name(), line.value, line.isReference()))
            }
        }
        return attributes
    }

    private fun <T> asTable(chunk: TsvChunk, initializer: (String, MutableList<Attribute>) -> T): List<T> {
        val rows: MutableList<T> = mutableListOf()

        chunk.lines.forEach {
            val attrs: MutableList<Attribute> = mutableListOf()
            it.values.forEachIndexed { index, attr -> attrs.add(Attribute(chunk.header[index + 1], attr)) }
            rows.add(initializer(it.name(), attrs))
        }

        return rows.toList()
    }
}
