package ac.uk.ebi.biostd.tsv.deserialization

import ac.uk.ebi.biostd.tsv.TSV_SEPARATOR
import ac.uk.ebi.biostd.tsv.deserialization.ext.findIdentifier
import ac.uk.ebi.biostd.tsv.deserialization.ext.getIdentifier
import ac.uk.ebi.biostd.tsv.deserialization.ext.getParent
import ac.uk.ebi.biostd.tsv.deserialization.ext.getType
import ac.uk.ebi.biostd.tsv.deserialization.ext.isNameDetail
import ac.uk.ebi.biostd.tsv.deserialization.ext.isReference
import ac.uk.ebi.biostd.tsv.deserialization.ext.isSectionTable
import ac.uk.ebi.biostd.tsv.deserialization.ext.isSubsection
import ac.uk.ebi.biostd.tsv.deserialization.ext.isValueDetail
import ac.uk.ebi.biostd.tsv.deserialization.ext.name
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
import ebi.ac.uk.model.constans.FileFields
import ebi.ac.uk.model.constans.LinkFields
import ebi.ac.uk.model.constans.SectionFields
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.util.collections.filterLeft
import ebi.ac.uk.util.collections.ifLeft

class ChunkProcessor {

    fun getSubmission(tsvChunk: TsvChunk): Submission {
        requireType(SubFields.SUBMISSION.value, tsvChunk)
        return Submission(
            accNo = tsvChunk.findIdentifier().orEmpty(),
            attributes = toAttributes(tsvChunk.lines)
        )
    }

    fun getRootSection(tsvChunk: TsvChunk): Section {
        requireType("Study", tsvChunk)

        return Section(
            accNo = tsvChunk.findIdentifier(),
            type = tsvChunk.getType(),
            attributes = toAttributes(tsvChunk.lines))
    }

    fun processChunk(section: Section, chunk: TsvChunk) {
        when (chunk.getType().toLowerCase()) {
            LinkFields.LINK.value ->
                section.addLink(Link(chunk.getIdentifier(), toAttributes(chunk.lines)))

            FileFields.FILE.value ->
                section.addFile(File(chunk.getIdentifier(), toAttributes(chunk.lines)))

            SectionFields.LINKS.value ->
                section.addLinksTable(LinksTable(asTable(chunk) { url, attributes -> Link(url, attributes) }))

            SectionFields.FILES.value ->
                section.addFilesTable(FilesTable(asTable(chunk) { path, attributes -> File(path, attributes) }))

            else -> processSectionChunk(section, chunk)
        }
    }

    private fun processSectionChunk(section: Section, chunk: TsvChunk) =
        when {
            chunk.isSubsection() -> addSubsection(section, chunk)
            chunk.isSectionTable() -> section.addSectionTable(createSectionsTable(chunk))
            else -> section.addSection(createSingleSection(chunk))
        }

    private fun createSectionsTable(chunk: TsvChunk) =
        SectionsTable(asTable(chunk) { accNo, attributes -> Section(accNo = accNo, attributes = attributes) }
            .map { it.apply { type = chunk.getType() } })

    private fun addSubsection(parentSection: Section, sectionChunk: TsvChunk) {
        if (parentSection.accNo == sectionChunk.getParent()) {
            processSubSectionChunk(parentSection, sectionChunk)
        } else {
            parentSection.sections
                .filterLeft { section -> section.accNo == sectionChunk.getParent() }
                .first()
                .ifLeft { section -> processSubSectionChunk(section, sectionChunk) }
        }
    }

    private fun processSubSectionChunk(parentSection: Section, sectionChunk: TsvChunk) {
        if (sectionChunk.isSectionTable()) parentSection.addSectionTable(createSectionsTable(sectionChunk))
        else parentSection.addSection(createSingleSection(sectionChunk))
    }

    private fun createSingleSection(chunk: TsvChunk) =
        Section(type = chunk.getType(), accNo = chunk.getIdentifier(), attributes = toAttributes(chunk.lines))


    private fun requireType(type: String, tsvChunk: TsvChunk) {
        require(tsvChunk.getType().equals(type, ignoreCase = true)) { "Expected to find block type of $type in block $tsvChunk" }
    }

    private fun toAttributes(chunkLines: MutableList<TsvChunkLine>): MutableList<Attribute> {
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
            it.value.split(TSV_SEPARATOR).forEachIndexed { index, attr -> attrs.add(Attribute(chunk.header[index + 1], attr)) }
            rows.add(initializer(it.name(), attrs))
        }

        return rows.toList()
    }
}
