package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import ac.uk.ebi.biostd.tsv.serialization.TsvSerializer
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Submission
import java.io.InputStream
import java.io.OutputStream

internal class TsvSerializer(
    private val tsvSerializer: TsvSerializer = TsvSerializer(),
    private val tsvDeserializer: TsvDeserializer = TsvDeserializer(),
    private val streamSerializer: FileListTsvStreamDeserializer = FileListTsvStreamDeserializer()
) {
    fun serializeSubmission(element: Submission): String = tsvSerializer.serialize(element)

    fun deserializeSubmission(pageTab: String): Submission = tsvDeserializer.deserialize(pageTab)

    fun serializeFileList(
        files: Sequence<BioFile>,
        outputStream: OutputStream
    ) = streamSerializer.serializeFileList(files, outputStream)

    fun deserializeFileList(
        inputStream: InputStream
    ): Sequence<BioFile> = streamSerializer.deserializeFileList(inputStream)
}
