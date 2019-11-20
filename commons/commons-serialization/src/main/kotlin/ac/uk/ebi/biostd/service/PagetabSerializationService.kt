package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.JsonFormat
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.integration.SubFormat.Companion.fromExtension
import ac.uk.ebi.biostd.integration.Tsv
import ac.uk.ebi.biostd.integration.XlsxTsv
import ac.uk.ebi.biostd.integration.XmlFormat
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.file.ExcelReader
import java.io.File

internal class PagetabSerializationService(
    private val serializer: PagetabSerializer = PagetabSerializer(),
    private val fileListSerializer: FileListSerializer = FileListSerializer(serializer),
    private val excelReader: ExcelReader = ExcelReader()
) : SerializationService {

    override fun <T> serializeElement(element: T, format: SubFormat) =
        serializer.serializeElement(element, format)

    override fun serializeSubmission(submission: Submission, format: SubFormat) =
        serializer.serializeSubmission(submission, format)

    override fun deserializeSubmission(content: String, format: SubFormat): Submission =
        serializer.deserializeSubmission(content, format)

    override fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission =
        fileListSerializer.deserializeFileList(serializer.deserializeSubmission(content, format), format, source)

    override fun deserializeSubmission(file: File): Submission =
        when (fromExtension(file.extension)) {
            XmlFormat -> deserializeSubmission(file.readText(), XML)
            is JsonFormat -> deserializeSubmission(file.readText(), JSON)
            Tsv -> deserializeSubmission(file.readText(), TSV)
            XlsxTsv -> deserializeSubmission(excelReader.readContentAsTsv(file), TSV)
        }

    override fun deserializeSubmission(file: File, source: FilesSource): Submission =
        fileListSerializer.deserializeFileList(deserializeSubmission(file), fromExtension(file.extension), source)
}
