package ac.uk.ebi.biostd.tsv.deserialization

import ac.uk.ebi.biostd.tsv.TSV_CHUNK_BREAK
import ac.uk.ebi.biostd.tsv.TSV_LINE_BREAK
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.collections.ifNotEmpty
import ebi.ac.uk.util.collections.removeFirst

class TsvDeserializer(private val chunkProcessor: ChunkProcessor = ChunkProcessor()) {

    fun deserialize(pageTabSubmission: String): Submission {
        val chunks: MutableList<TsvChunk> = chunkerize(pageTabSubmission)

        return chunkProcessor.getSubmission(chunks.removeFirst()).apply {
            chunks.ifNotEmpty {
                section = chunkProcessor.getRootSection(chunks.removeFirst())
                chunks.forEach { chunk -> chunkProcessor.processChunk(section, chunk) }
            }
        }
    }

    private fun chunkerize(submissionPageTab: String) =
        submissionPageTab.split(TSV_LINE_BREAK)
            .asSequence()
            .map { chunk -> chunk.split(TSV_CHUNK_BREAK).filterTo(mutableListOf(), String::isNotEmpty) }
            .mapTo(mutableListOf()) { chunks -> TsvChunk(chunks) }
}
