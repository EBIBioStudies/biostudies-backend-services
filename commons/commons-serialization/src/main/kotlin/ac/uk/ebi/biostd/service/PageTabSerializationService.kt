package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.service.PageTabFileReader.readAsPageTab
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import java.io.File

internal class PageTabSerializationService(
    private val serializer: PagetabSerializer,
    private val fileListSerializer: FileListSerializer
) : SerializationService {
    override fun <T> serializeElement(element: T, format: SubFormat) =
        serializer.serializeElement(element, format)

    override fun serializeFileList(filesTable: FilesTable, format: SubFormat) =
        serializer.serializeFileList(filesTable, format)

    override fun serializeSubmission(submission: Submission, format: SubFormat) =
        serializer.serializeSubmission(submission, format)

    override fun deserializeSubmission(content: String, format: SubFormat): Submission =
        serializer.deserializeSubmission(content, format)

    override fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission =
        fileListSerializer.deserializeFileList(serializer.deserializeSubmission(content, format), source)

    override fun deserializeFileList(fileName: String, source: FilesSource): FileList =
        fileListSerializer.deserializeFileList(fileName, source)

    override fun deserializeSubmission(file: File): Submission =
        deserializeSubmission(readAsPageTab(file), SubFormat.fromFile(file))

    override fun deserializeSubmission(file: File, source: FilesSource): Submission =
        fileListSerializer.deserializeFileList(deserializeSubmission(file), source)
}
