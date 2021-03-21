package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.XlsxTsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.file.ExcelReader
import java.io.File

internal class PageTabSerializationService(
    private val excelReader: ExcelReader,
    private val serializer: PagetabSerializer,
    private val fileListSerializer: FileListSerializer
) : SerializationService {
    override fun <T> serializeElement(element: T, format: SubFormat) =
        serializer.serializeElement(element, format)

    override fun serializeSubmission(submission: Submission, format: SubFormat) =
        serializer.serializeSubmission(submission, format)

    override fun deserializeSubmission(content: String, format: SubFormat): Submission =
        serializer.deserializeSubmission(content, format)

    override fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission =
        fileListSerializer.deserializeFileList(serializer.deserializeSubmission(content, format), source)

    override fun deserializeSubmission(file: File): Submission =
        when (SubFormat.fromFile(file)) {
            XmlFormat -> deserializeSubmission(file.readText(), XML)
            is JsonFormat -> deserializeSubmission(file.readText(), JSON)
            Tsv -> deserializeSubmission(file.readText(), TSV)
            XlsxTsv -> deserializeSubmission(excelReader.readContentAsTsv(file), TSV)
        }

    override fun deserializeSubmission(file: File, source: FilesSource): Submission =
        fileListSerializer.deserializeFileList(deserializeSubmission(file), source)
}
