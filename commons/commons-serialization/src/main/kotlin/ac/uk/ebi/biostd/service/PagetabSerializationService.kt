package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.InvalidExtensionException
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import java.io.File

internal class PagetabSerializationService(
    private val serializer: PagetabSerializer = PagetabSerializer(),
    private val fileListSerializer: FileListSerializer = FileListSerializer(serializer)
) : SerializationService {
    override fun <T> serializeElement(element: T, format: SubFormat) =
        serializer.serializeElement(element, format)

    override fun serializeSubmission(submission: Submission, format: SubFormat) =
        serializer.serializeSubmission(submission, format)

    override fun deserializeSubmission(content: String, format: SubFormat): Submission =
        serializer.deserializeSubmission(content, format)

    override fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission =
        fileListSerializer.deserializeFileList(serializer.deserializeSubmission(content, format), format, source)

    override fun getSubmissionFormat(file: File) = when (file.extension) {
        "tsv" -> SubFormat.TSV
        "xlsx" -> SubFormat.TSV
        "xml" -> SubFormat.XML
        "json" -> SubFormat.JSON
        else -> throw InvalidExtensionException(file)
    }
}
