package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.PlainJson
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat
import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

internal class PagetabSerializer(
    private val jsonSerializer: JsonSerializer = JsonSerializer(),
    private val tsvSerializer: TsvSerializer,
) {
    fun serializeSubmission(
        submission: Submission,
        format: SubFormat,
    ): String =
        when (format) {
            PlainJson -> jsonSerializer.serialize(submission)
            JsonPretty -> jsonSerializer.serialize(submission, true)
            is TsvFormat -> tsvSerializer.serializeSubmission(submission)
        }

    fun deserializeSubmission(
        submission: String,
        format: SubFormat,
    ): Submission =
        when (format) {
            is JsonFormat -> jsonSerializer.deserialize(submission)
            is TsvFormat -> tsvSerializer.deserializeSubmission(submission)
        }

    suspend fun serializeFileList(
        files: Flow<BioFile>,
        format: SubFormat,
        outputStream: OutputStream,
    ) {
        when (format) {
            JsonPretty, PlainJson -> jsonSerializer.serializeFileList(files, outputStream)
            is TsvFormat -> tsvSerializer.serializeFileList(files, outputStream)
        }
    }

    suspend fun serializeLinkList(
        files: Flow<Link>,
        format: SubFormat,
        outputStream: OutputStream,
    ) {
        when (format) {
            JsonPretty, PlainJson -> jsonSerializer.serializeLinkList(files, outputStream)
            is TsvFormat -> tsvSerializer.serializeLinkList(files, outputStream)
        }
    }

    fun deserializeFileListAsFlow(
        input: InputStream,
        format: SubFormat,
    ): Flow<BioFile> =
        when (format) {
            is JsonFormat -> jsonSerializer.deserializeFileList(input)
            is TsvFormat -> tsvSerializer.deserializeFileList(input)
        }

    fun deserializeLinkListAsFlow(
        input: InputStream,
        format: SubFormat,
    ): Flow<Link> =
        when (format) {
            is JsonFormat -> jsonSerializer.deserializeLinkList(input)
            is TsvFormat -> tsvSerializer.deserializeLinkList(input)
        }
}
