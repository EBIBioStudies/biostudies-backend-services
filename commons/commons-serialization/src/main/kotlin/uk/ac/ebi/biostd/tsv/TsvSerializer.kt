package uk.ac.ebi.biostd.tsv

import kotlinx.coroutines.flow.Flow
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.Link
import uk.ac.ebi.biostd.model.Submission
import uk.ac.ebi.biostd.tsv.deserialization.TsvDeserializer
import uk.ac.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import uk.ac.ebi.biostd.tsv.deserialization.stream.LinkListTsvStreamDeserializer
import uk.ac.ebi.biostd.tsv.serialization.TsvSerializer
import java.io.InputStream
import java.io.OutputStream

internal class TsvSerializer(
    private val tsvSerializer: TsvSerializer,
    private val tsvDeserializer: TsvDeserializer = TsvDeserializer(),
    private val streamSerializer: FileListTsvStreamDeserializer = FileListTsvStreamDeserializer(),
    private val linkStreamSerializer: LinkListTsvStreamDeserializer = LinkListTsvStreamDeserializer(),
) {
    fun serializeSubmission(element: Submission): String = tsvSerializer.serialize(element)

    suspend fun serializeFileList(
        files: Flow<BioFile>,
        outputStream: OutputStream,
    ): Unit = streamSerializer.serializeFileList(files, outputStream)

    suspend fun serializeLinkList(
        links: Flow<Link>,
        outputStream: OutputStream,
    ): Unit = linkStreamSerializer.serializeLinkList(links, outputStream)

    fun deserializeSubmission(pageTab: String): Submission = tsvDeserializer.deserialize(pageTab)

    fun deserializeFileList(inputStream: InputStream): Flow<BioFile> = streamSerializer.deserializeFileList(inputStream)

    fun deserializeLinkList(inputStream: InputStream): Flow<Link> = linkStreamSerializer.deserializeLinkList(inputStream)
}
