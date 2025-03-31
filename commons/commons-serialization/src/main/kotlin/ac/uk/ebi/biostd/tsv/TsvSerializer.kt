package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import ac.uk.ebi.biostd.tsv.deserialization.stream.LinkListTsvStreamDeserializer
import ac.uk.ebi.biostd.tsv.serialization.TsvSerializer
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.flow.Flow
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

    fun deserializeLinkList(inputStream: InputStream): Flow<Link> = streamSerializer.deserializeLinkList(inputStream)
}
