package ac.uk.ebi.biostd.serialization.tsv

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
        val headerValues: List<String> = submissionChunk.getHeaderValues()
        var rootSection = Section()
        chunks.ifNotEmpty {
            val rootSectionChunk: PageTabChunk = chunks.removeFirst()
            rootSection = Section(type = rootSectionChunk.header, attributes = rootSectionChunk.attributes)
            processSubsections(rootSection, chunks)
        }

        return Submission(
                accNo = headerValues[1],
                title = submissionChunk.attributes.removeFirst().value,
                accessTags = if (headerValues.size > 2) mutableListOf(headerValues[2]) else mutableListOf(),
                attributes = submissionChunk.attributes,
                section = rootSection)
    }

    private fun processSubsections(section: Section, subsectionChunks: MutableList<PageTabChunk>) {
        subsectionChunks.forEach {
            val head: List<String> = it.getHeaderValues()
            val type: String = head[0].toLowerCase()

            when (type) {
                LinkFields.LINK.value -> section.links.addLeft(Link(head[1], it.attributes))
                FileFields.FILE.value -> section.files.addLeft(File(name =  head[1], attributes = it.attributes))
                SectionFields.LINKS.value -> section.links.addRight(LinksTable(it.mapTable(this::createLink)))
                SectionFields.FILES.value -> section.files.addRight(FilesTable(it.mapTable(this::createFile)))
                else -> section.subsections.addLeft(Section(type = it.header, attributes = it.attributes))
            }
        }
    }

    private fun chunkerize(pageTabSubmission: String): MutableList<PageTabChunk> {
        var chunk: MutableList<String> = arrayListOf()
        val chunks: MutableList<PageTabChunk> = arrayListOf()
        pageTabSubmission.split("\n\n").forEach {
            it.split("\n"). forEach {
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
