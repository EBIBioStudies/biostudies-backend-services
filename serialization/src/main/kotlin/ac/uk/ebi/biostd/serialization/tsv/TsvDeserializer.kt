package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.serialization.common.TSV_CHUNK_BREAK
import ac.uk.ebi.biostd.serialization.common.TSV_LINE_BREAK
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
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.addLeft
import ebi.ac.uk.util.collections.filterLeft
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifNotEmpty
import ebi.ac.uk.util.collections.removeFirst

class TsvDeserializer {
    fun deserialize(pageTabSubmission: String): Submission {
        val chunks: MutableList<TsvChunk> = chunkerize(pageTabSubmission)
        val submissionChunk: TsvChunk = chunks.removeFirst()
        var rootSection = Section()
        chunks.ifNotEmpty {
            val rootSectionChunk: TsvChunk = chunks.removeFirst()
            rootSection = Section(
                    attributes = createAttributes(rootSectionChunk.lines))
            rootSection.type = rootSectionChunk.getType()
            processSubsections(rootSection, chunks)
        }

        val submissionTitle = submissionChunk.lines.removeFirst().value
        val submission = Submission(
                accNo = submissionChunk.getIdentifier(),
                rootSection = rootSection,
                attributes = createAttributes(submissionChunk.lines))

        submission.title = submissionTitle

        return submission
    }

    private fun chunkerize(pageTabSubmission: String): MutableList<TsvChunk> {
        return pageTabSubmission.split(TSV_LINE_BREAK)
                .asSequence()
                .map { chunk -> chunk.split(TSV_CHUNK_BREAK).filterTo(mutableListOf(), String::isNotEmpty) }
                .mapTo(mutableListOf()) { chunk -> TsvChunk(chunk) }
    }

    private fun processSubsections(section: Section, subsectionChunks: MutableList<TsvChunk>) {
        subsectionChunks.forEach {
            when (it.getType().toLowerCase()) {
                LinkFields.LINK.value -> section.addLink(Link(it.getIdentifier(), createAttributes(it.lines)))
                FileFields.FILE.value ->
                    section.addFile(File(name = it.getIdentifier(), attributes = createAttributes(it.lines)))
                SectionFields.LINKS.value -> section.addLinksTable(LinksTable(it.mapTable(this::createLink)))
                SectionFields.FILES.value -> section.addFilesTable(FilesTable(it.mapTable(this::createFile)))
                else -> processSubsection(section, it)
            }
        }
    }

    private fun processSubsection(parentSection: Section, subsectionChunk: TsvChunk) {
        when {
            subsectionChunk.isSectionTable() -> {
                val subsections = subsectionChunk.mapTable(this::createTableSection)

                subsections.forEach { it.type = subsectionChunk.getType() }
                parentSection.addSectionTable(SectionsTable(subsections))
            }
            subsectionChunk.isSubsection() -> {
                parentSection.sections
                    .filterLeft { section -> section.accNo == subsectionChunk.getParent() }
                    .first()
                    .ifLeft { section ->
                        section.sections.addLeft(Section(
                            type = subsectionChunk.getType(),
                            accNo = subsectionChunk.getIdentifier(),
                            attributes = createAttributes(subsectionChunk.lines)))
                    }
            }
            else -> parentSection.addSection(createSingleSection(subsectionChunk))
        }
    }

    private fun createSingleSection(sectionChunk: TsvChunk) =
            Section(
                    type = sectionChunk.getType(),
                    accNo = sectionChunk.getIdentifier(),
                    attributes = createAttributes(sectionChunk.lines))

    private fun createTableSection(
            accNo: String, attributes: MutableList<Attribute>) = Section(accNo = accNo, attributes = attributes)

    private fun createLink(link: String, attributes: MutableList<Attribute>): Link = Link(link, attributes)

    private fun createFile(
            file: String, attributes: MutableList<Attribute>): File = File(name = file, attributes = attributes)

    private fun createAttributes(chunkLines: MutableList<TsvChunkLine>): MutableList<Attribute> {
        val attributes: MutableList<Attribute> = mutableListOf()
        chunkLines.forEach {
            when {
                it.isNameDetail() -> attributes.last().nameAttrs.add(AttributeDetail(it.name(), it.value))
                it.isValueDetail() -> attributes.last().valueAttrs.add(AttributeDetail(it.name(), it.value))
                else -> attributes.add(Attribute(it.name(), it.value, it.isReference()))
            }
        }

        return attributes
    }
}
