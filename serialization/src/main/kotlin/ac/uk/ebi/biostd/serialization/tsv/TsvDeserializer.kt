package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.serialization.common.TSV_CHUNK_BREAK
import ac.uk.ebi.biostd.serialization.common.TSV_LINE_BREAK
import ac.uk.ebi.biostd.serialization.common.addLeft
import ac.uk.ebi.biostd.serialization.common.addRight
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.FileFields
import ac.uk.ebi.biostd.submission.FilesTable
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.LinkFields
import ac.uk.ebi.biostd.submission.LinksTable
import ac.uk.ebi.biostd.submission.Section
import ac.uk.ebi.biostd.submission.SectionFields
import ac.uk.ebi.biostd.submission.Submission
import ebi.ac.uk.base.applyIfNotBlank
import ebi.ac.uk.util.collections.removeFirst
import ebi.ac.uk.util.collections.ifNotEmpty

class TsvDeserializer {
    fun deserialize(pageTabSubmission: String): Submission {
        val chunks: MutableList<PageTabChunk> = chunkerize(pageTabSubmission)
        val submissionChunk: PageTabChunk = chunks.removeFirst()
        var rootSection = Section()
        chunks.ifNotEmpty {
            val rootSectionChunk: PageTabChunk = chunks.removeFirst()
            rootSection = Section(type = rootSectionChunk.getType(), attributes = rootSectionChunk.attributes)
            processSubsections(rootSection, chunks)
        }

        return Submission(
                accNo = submissionChunk.getIdentifier(),
                title = submissionChunk.attributes.removeFirst().value,
                attributes = submissionChunk.attributes,
                section = rootSection)
    }

    private fun processSubsections(section: Section, subsectionChunks: MutableList<PageTabChunk>) {
        subsectionChunks.forEach {
            when (it.getType()) {
                LinkFields.LINK.value -> section.links.addLeft(Link(it.getIdentifier(), it.attributes))
                FileFields.FILE.value ->
                    section.files.addLeft(File(name =  it.getIdentifier(), attributes = it.attributes))
                SectionFields.LINKS.value -> section.links.addRight(LinksTable(it.mapTable(this::createLink)))
                SectionFields.FILES.value -> section.files.addRight(FilesTable(it.mapTable(this::createFile)))
                else -> section.subsections.addLeft(Section(type = it.getType(), attributes = it.attributes))
            }
        }
    }

    private fun chunkerize(pageTabSubmission: String): MutableList<PageTabChunk> {
        var chunk: MutableList<String> = arrayListOf()
        val chunks: MutableList<PageTabChunk> = arrayListOf()
        pageTabSubmission.split(TSV_LINE_BREAK).forEach {
            it.split(TSV_CHUNK_BREAK). forEach {
                it.applyIfNotBlank { chunk.add(it) }
            }

            chunks.add(PageTabChunk(chunk))
            chunk.clear()
        }

        return chunks
    }

    private fun createLink(link:String, attributes: MutableList<Attribute>): Link = Link(link, attributes)

    private fun createFile(
            file: String, attributes: MutableList<Attribute>): File = File(name = file, attributes = attributes)
}
