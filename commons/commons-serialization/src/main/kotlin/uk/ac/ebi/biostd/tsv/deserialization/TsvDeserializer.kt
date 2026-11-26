package uk.ac.ebi.biostd.tsv.deserialization

import uk.ac.ebi.biostd.model.Submission
import uk.ac.ebi.biostd.tsv.deserialization.chunks.ChunkProcessor
import uk.ac.ebi.biostd.tsv.deserialization.chunks.TsvChunkGenerator
import uk.ac.ebi.biostd.util.collections.ifNotEmpty
import uk.ac.ebi.biostd.validation.InvalidChunkSizeException

internal class TsvDeserializer(
    private val chunkProcessor: ChunkProcessor = ChunkProcessor(),
    private val chunkGenerator: TsvChunkGenerator = TsvChunkGenerator(),
) {
    fun deserialize(pageTab: String): Submission {
        val chunks = chunkGenerator.chunks(pageTab)
        val context = TsvSerializationContext()

        context.addSubmission(chunks.poll()) { chunk -> chunkProcessor.getSubmission(chunk) }
        chunks.ifNotEmpty {
            context.addRootSection(chunks.poll()) { chunk -> chunkProcessor.getRootSection(chunk) }
            chunks.forEach { chunk -> chunkProcessor.processChunk(chunk, context) }
        }

        return context.getSubmission()
    }

    inline fun <reified T> deserializeElement(pageTab: String): T {
        return deserializeElement(pageTab, T::class.java)
    }

    fun <T> deserializeElement(
        pageTab: String,
        type: Class<out T>,
    ): T {
        val chunks = chunkGenerator.chunks(pageTab)
        require(chunks.size == 1) { throw InvalidChunkSizeException() }
        return type.cast(chunkProcessor.processIsolatedChunk(chunks.poll()))
    }
}
