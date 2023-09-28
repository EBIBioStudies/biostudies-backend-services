package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.PlainJson
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import ac.uk.ebi.biostd.xml.XmlStreamSerializer
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

internal class PagetabSerializer(
    private val jsonSerializer: JsonSerializer = JsonSerializer(),
    private val xmlSerializer: XmlSerializer = XmlSerializer(),
    private val xmlStreamSerializer: XmlStreamSerializer = XmlStreamSerializer(),
    private val tsvSerializer: TsvSerializer,
) {
    fun serializeSubmission(submission: Submission, format: SubFormat): String = when (format) {
        XmlFormat -> xmlSerializer.serialize(submission)
        PlainJson -> jsonSerializer.serialize(submission)
        JsonPretty -> jsonSerializer.serialize(submission, true)
        is TsvFormat -> tsvSerializer.serializeSubmission(submission)
    }

    fun deserializeSubmission(submission: String, format: SubFormat): Submission = when (format) {
        XmlFormat -> xmlSerializer.deserialize(submission)
        is JsonFormat -> jsonSerializer.deserialize(submission)
        is TsvFormat -> tsvSerializer.deserializeSubmission(submission)
    }

    fun serializeFileList(files: Sequence<BioFile>, format: SubFormat, outputStream: OutputStream) {
        when (format) {
            XmlFormat -> xmlStreamSerializer.serializeFileList(files, outputStream)
            JsonPretty, PlainJson -> jsonSerializer.serializeFileList(files, outputStream)
            is TsvFormat -> tsvSerializer.serializeFileList(files, outputStream)
        }
    }

    suspend fun serializeFileList(files: Flow<BioFile>, format: SubFormat, outputStream: OutputStream) {
        when (format) {
            XmlFormat -> xmlStreamSerializer.serializeFileList(files, outputStream)
            JsonPretty, PlainJson -> jsonSerializer.serializeFileList(files, outputStream)
            is TsvFormat -> tsvSerializer.serializeFileList(files, outputStream)
        }
    }

    fun deserializeFileList(input: InputStream, format: SubFormat): Sequence<BioFile> {
        return when (format) {
            XmlFormat -> xmlStreamSerializer.deserializeFileList(input)
            is JsonFormat -> jsonSerializer.deserializeFileList(input)
            is TsvFormat -> tsvSerializer.deserializeFileList(input)
        }
    }
}
