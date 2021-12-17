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
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.file.ExcelReader

internal class PagetabSerializer(
    private val jsonSerializer: JsonSerializer = JsonSerializer(),
    private val xmlSerializer: XmlSerializer = XmlSerializer(),
    private val tsvSerializer: TsvSerializer = TsvSerializer()
) {
    fun serializeSubmission(submission: Submission, format: SubFormat): String = serializeElement(submission, format)

    fun <T> serializeElement(element: T, format: SubFormat) = when (format) {
        XmlFormat -> xmlSerializer.serialize(element)
        PlainJson -> jsonSerializer.serialize(element)
        JsonPretty -> jsonSerializer.serialize(element, true)
        is TsvFormat -> tsvSerializer.serialize(element)
    }

    fun deserializeSubmission(submission: String, format: SubFormat) = when (format) {
        XmlFormat -> xmlSerializer.deserialize(submission)
        is JsonFormat -> jsonSerializer.deserialize(submission)
        is TsvFormat -> tsvSerializer.deserialize(submission)
    }

    fun serializeFileList(filesTable: FilesTable, format: SubFormat): String = when (format) {
        XmlFormat -> xmlSerializer.serialize(filesTable)
        JsonPretty, PlainJson -> jsonSerializer.serializeFileList(filesTable.elements).readText()
        is TsvFormat -> tsvSerializer.serializeFileList(filesTable.elements).readText()
    }

    fun deserializeFileList(file: java.io.File, format: SubFormat): FilesTable {
        return when (format) {
            XmlFormat -> xmlSerializer.deserialize(file.readText(), FilesTable::class.java)
            is JsonFormat -> FilesTable(jsonSerializer.deserializeFileList(file))
            is XlsxTsv -> tsvSerializer.deserializeElement(ExcelReader.readContentAsTsv(file), FilesTable::class.java)
            is TsvFormat -> FilesTable(tsvSerializer.deserializeFileList(file))
        }
    }
}
