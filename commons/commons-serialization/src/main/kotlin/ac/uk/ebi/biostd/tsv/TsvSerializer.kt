package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import ac.uk.ebi.biostd.tsv.serialization.TsvToStringSerializer
import ebi.ac.uk.model.Submission
import java.io.File

internal class TsvSerializer(
    private val tsvSerializer: TsvToStringSerializer = TsvToStringSerializer(),
    private val tsvDeserializer: TsvDeserializer = TsvDeserializer(),
    private val streamSerializer: FileListTsvStreamDeserializer = FileListTsvStreamDeserializer()
) {
    fun serializeSubmission(element: Submission): String = tsvSerializer.serialize(element)

    fun deserializeSubmission(pageTab: String): Submission = tsvDeserializer.deserialize(pageTab)

    fun serializeFileList(files: List<ebi.ac.uk.model.File>, file: File) =
        streamSerializer.serializeFileList(files.asSequence(), file)

    fun deserializeFileList(file: File): List<ebi.ac.uk.model.File> =
        streamSerializer.deserializeFileList(file).toList()
}
