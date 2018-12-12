package ac.uk.ebi.biostd.tsv.deserialization

import ac.uk.ebi.biostd.tsv.TSV_CHUNK_BREAK
import ac.uk.ebi.biostd.tsv.TSV_LINE_BREAK
import ac.uk.ebi.biostd.tsv.deserialization.model.FileChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.FileTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinkChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinksTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSubSectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SectionContext
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

        return chunkProcessor.getSubmission(chunks.removeFirst()).apply {
            chunks.ifNotEmpty {
                section = chunkProcessor.getRootSection(chunks.removeFirst())
                val context = SectionContext(section)
                chunks.forEach { chunk -> chunkProcessor.processChunk(chunk, context) }
            }
        }
    }

    private fun chunkerize(pagetab: String) =
        pagetab.splitIgnoringEmpty(TSV_LINE_BREAK)
            .asSequence()
            .map { chunk -> chunk.splitIgnoringEmpty(TSV_CHUNK_BREAK) }
            .mapTo(mutableListOf()) { lines -> createChunk(lines) }

    private fun createChunk(body: List<String>): TsvChunk {
        val header = TsvChunkLine(body.first())
        val type = header.first()

        return when {
            type like LinkFields.LINK -> LinkChunk(body)
            type like FileFields.FILE -> FileChunk(body)
            type like SectionFields.LINKS -> LinksTableChunk(body)
            type like SectionFields.FILES -> FileTableChunk(body)
            type.matches(TABLE_REGEX) -> TABLE_REGEX.findGroup(type, 1).fold({ RootSectionTableChunk(body) }, { SubSectionTableChunk(body, it) })
            else -> header.findThird().fold({ RootSubSectionChunk(body) }, { SubSectionChunk(body, it) })
        }
    }
}
