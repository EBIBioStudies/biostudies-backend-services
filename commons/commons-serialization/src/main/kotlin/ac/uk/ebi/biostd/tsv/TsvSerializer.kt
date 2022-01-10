package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import ac.uk.ebi.biostd.tsv.deserialization.stream.PageTabFile
import ac.uk.ebi.biostd.tsv.serialization.TsvToStringSerializer
import ebi.ac.uk.model.Submission
import java.io.InputStream
import java.io.OutputStream

internal class TsvSerializer(
    private val tsvSerializer: TsvToStringSerializer = TsvToStringSerializer(),
    private val tsvDeserializer: TsvDeserializer = TsvDeserializer(),
    private val streamSerializer: FileListTsvStreamDeserializer = FileListTsvStreamDeserializer()
) {
    fun serializeSubmission(element: Submission): String = tsvSerializer.serialize(element)

    fun deserializeSubmission(pageTab: String): Submission = tsvDeserializer.deserialize(pageTab)

    fun serializeFileList(files: Sequence<PageTabFile>, outputStream: OutputStream) =
        streamSerializer.serializeFileList(files, outputStream)

    fun deserializeFileList(inputStream: InputStream): Sequence<PageTabFile> =
        streamSerializer.deserializeFileList(inputStream)
}
