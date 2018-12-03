package ac.uk.ebi.biostd.tsv.deserialization

import ac.uk.ebi.biostd.tsv.TSV_CHUNK_BREAK
import ac.uk.ebi.biostd.tsv.TSV_LINE_BREAK
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
                accNo = rootSectionChunk.getIdentifier(),
                type = rootSectionChunk.getType(),
                attributes = createAttributes(rootSectionChunk.lines))

            processChunks(rootSection, chunks)
        }

        val submissionTitle = submissionChunk.lines.removeFirst().value
        val submission = Submission(
            accNo = submissionChunk.getIdentifier(),
            section = rootSection,
            attributes = createAttributes(submissionChunk.lines))

        submission.title = submissionTitle

        return submission
    }

    private fun chunkerize(pageTabSubmission: String) =
        pageTabSubmission.split(TSV_LINE_BREAK)
            .asSequence()
            .map { chunk -> chunk.split(TSV_CHUNK_BREAK).filterTo(mutableListOf(), String::isNotEmpty) }
            .mapTo(mutableListOf()) { chunk -> TsvChunk(chunk) }

    private fun processChunks(rootSection: Section, chunks: MutableList<TsvChunk>) =
        chunks.forEach {
            when (it.getType().toLowerCase()) {
                LinkFields.LINK.value -> rootSection.addLink(Link(it.getIdentifier(), createAttributes(it.lines)))

                FileFields.FILE.value ->
                    rootSection.addFile(File(name = it.getIdentifier(), attributes = createAttributes(it.lines)))

                SectionFields.LINKS.value ->
                    rootSection.addLinksTable(LinksTable(it.mapTable { url, attributes -> Link(url, attributes) }))

                SectionFields.FILES.value ->
                    rootSection.addFilesTable(FilesTable(
                        it.mapTable { path, attributes -> File(name = path, attributes = attributes) }))

                else -> processSectionChunk(rootSection, it)
            }
        }

    private fun processSectionChunk(rootSection: Section, chunk: TsvChunk) =
        when {
            chunk.isSubsection() -> addSubsection(rootSection, chunk)
            chunk.isSectionTable() -> rootSection.addSectionTable(createSectionsTable(chunk))
            else -> rootSection.addSection(createSingleSection(chunk))
        }

    private fun createSingleSection(chunk: TsvChunk) =
        Section(type = chunk.getType(), accNo = chunk.getIdentifier(), attributes = createAttributes(chunk.lines))

    private fun createSectionsTable(chunk: TsvChunk) =
        SectionsTable(chunk
            .mapTable { accNo, attributes -> Section(accNo = accNo, attributes = attributes) }
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
