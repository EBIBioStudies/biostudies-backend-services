package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.serialization.common.TSV_CHUNK_BREAK
import ac.uk.ebi.biostd.serialization.common.TSV_LINE_BREAK
import ac.uk.ebi.biostd.serialization.common.addLeft
import ac.uk.ebi.biostd.serialization.common.addRight
import ebi.ac.uk.base.applyIfNotBlank
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.FileFields
import ebi.ac.uk.model.constans.LinkFields
import ebi.ac.uk.model.constans.SectionFields
import ebi.ac.uk.model.extensions.Section
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
                    //type = rootSectionChunk.getType(),
                    attributes = createAttributes(rootSectionChunk.lines))
            processSubsections(rootSection, chunks)
        }

        return Submission(
                // accNo = submissionChunk.getIdentifier(),
                // title = submissionChunk.lines.removeFirst().value,
                attributes = createAttributes(submissionChunk.lines),
                rootSection = rootSection)
    }

    private fun processSubsections(section: Section, subsectionChunks: MutableList<TsvChunk>) {
        subsectionChunks.forEach {
            when (it.getType()) {
                LinkFields.LINK.value -> section.links.addLeft(Link(it.getIdentifier(), createAttributes(it.lines)))
                FileFields.FILE.value ->
                    section.files.addLeft(File(name = it.getIdentifier(), attributes = createAttributes(it.lines)))
                SectionFields.LINKS.value -> section.links.addRight(LinksTable(it.mapTable(this::createLink)))
                SectionFields.FILES.value -> section.files.addRight(FilesTable(it.mapTable(this::createFile)))
                else ->
                    section.sections.addLeft(Section(
                            //type = it.getType(),
                            attributes = createAttributes(it.lines)))
            }
        }
    }

    private fun chunkerize(pageTabSubmission: String): MutableList<TsvChunk> {
        var chunk: MutableList<String> = arrayListOf()
        val chunks: MutableList<TsvChunk> = arrayListOf()
        pageTabSubmission.split(TSV_LINE_BREAK).forEach {
            it.split(TSV_CHUNK_BREAK).forEach {
                it.applyIfNotBlank { chunk.add(it) }
            }

            chunks.add(TsvChunk(chunk))
            chunk.clear()
        }

        return chunks
    }

    private fun createLink(link: String, attributes: MutableList<Attribute>): Link = Link(link, attributes)

    private fun createFile(
            file: String, attributes: MutableList<Attribute>): File = File(name = file, attributes = attributes)

    private fun createAttributes(chunkLines: MutableList<TsvChunkLine>): MutableList<Attribute> =
            chunkLines.mapTo(mutableListOf()) { Attribute(it.name, it.value) }
}
