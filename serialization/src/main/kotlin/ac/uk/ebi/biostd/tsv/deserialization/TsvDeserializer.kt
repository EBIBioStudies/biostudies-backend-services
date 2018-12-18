package ac.uk.ebi.biostd.tsv.deserialization

import ac.uk.ebi.biostd.tsv.TSV_CHUNK_BREAK
import ac.uk.ebi.biostd.tsv.TSV_LINE_BREAK
import ac.uk.ebi.biostd.tsv.deserialization.model.FileChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.FileTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinkChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinksTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSubSectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SubSectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SubSectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ebi.ac.uk.base.like
import ebi.ac.uk.base.splitIgnoringEmpty
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.FileFields
import ebi.ac.uk.model.constans.LinkFields
import ebi.ac.uk.model.constans.SectionFields
import ebi.ac.uk.model.constans.TABLE_REGEX
import ebi.ac.uk.util.collections.findThird
import ebi.ac.uk.util.collections.ifNotEmpty
import ebi.ac.uk.util.collections.removeFirst
import ebi.ac.uk.util.regex.findGroup

class TsvDeserializer(private val chunkProcessor: ChunkProcessor = ChunkProcessor()) {

    fun deserialize(pagetab: String): Submission {
        val chunks: MutableList<TsvChunk> = chunkerize(pagetab)
        val context = TsvSerializationContext()

        context.addSubmission(chunks.removeFirst()) { chunk -> chunkProcessor.getSubmission(chunk) }
        chunks.ifNotEmpty {
            context.addRootSection(chunks.removeFirst()) { chunk -> chunkProcessor.getRootSection(chunk) }
            chunks.forEach { chunk -> chunkProcessor.processChunk(chunk, context) }
        }

        return context.getSubmission()
    }

    private fun chunkerize(pagetab: String) =
        pagetab.splitIgnoringEmpty(TSV_LINE_BREAK)
            .mapIndexedTo(mutableListOf()) { index, line -> createChunk(index, line.splitIgnoringEmpty(TSV_CHUNK_BREAK)) }

    private fun createChunk(index: Int, body: List<String>): TsvChunk {
        val header = TsvChunkLine(body.first())
        val type = header.first()

        return when {
            type like LinkFields.LINK -> LinkChunk(index, body)
            type like FileFields.FILE -> FileChunk(index, body)
            type like SectionFields.LINKS -> LinksTableChunk(index, body)
            type like SectionFields.FILES -> FileTableChunk(index, body)
            type.matches(TABLE_REGEX) -> TABLE_REGEX.findGroup(type, 1).fold(
                { RootSectionTableChunk(index, body) },
                { SubSectionTableChunk(index, body, it) })
            else -> header.findThird().fold({ RootSubSectionChunk(index, body) }, { SubSectionChunk(index, body, it) })
        }
    }
}
