package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.PlainJson
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.XlsxTsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import ac.uk.ebi.biostd.xml.XmlStreamSerializer
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.file.ExcelReader.readContentAsTsv
import java.io.InputStream
import java.io.OutputStream

internal class PagetabSerializer(
    private val jsonSerializer: JsonSerializer = JsonSerializer(),
    private val xmlSerializer: XmlSerializer = XmlSerializer(),
    private val xmlStreamSerializer: XmlStreamSerializer = XmlStreamSerializer(),
    private val tsvSerializer: TsvSerializer = TsvSerializer()
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

    fun serializeFileList(filesTable: FilesTable, format: SubFormat, outputStream: OutputStream) {
        when (format) {
            XmlFormat -> xmlStreamSerializer.serializeFileList(filesTable.elements.asSequence(), outputStream)
            JsonPretty, PlainJson -> jsonSerializer.serializeFileList(filesTable.elements.asSequence(), outputStream)
            is TsvFormat -> tsvSerializer.serializeFileList(filesTable.elements.asSequence(), outputStream)
        }
    }

    fun deserializeFileList(inputStream: InputStream, format: SubFormat): FilesTable {
        return when (format) {
            XmlFormat -> FilesTable(xmlStreamSerializer.deserializeFileList(inputStream).toList())
            is JsonFormat -> FilesTable(jsonSerializer.deserializeFileList(inputStream).toList())
            is XlsxTsv -> FilesTable(
                readContentAsTsv(inputStream).use { tsvSerializer.deserializeFileList(it).toList() }
            )
            is TsvFormat -> FilesTable(tsvSerializer.deserializeFileList(inputStream).toList())
        }
    }
}
