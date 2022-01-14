package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.service.PageTabFileReader.readAsPageTab
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import java.io.File
import java.io.InputStream
import ebi.ac.uk.model.File as PagetabFile

internal class PageTabSerializationService(
    private val serializer: PagetabSerializerImpl,
    private val fileListSerializer: FileListSerializer
) : SerializationService {
    override fun serializeSubmission(submission: Submission, format: SubFormat) =
        serializer.serializeSubmission(submission, format)

    override fun deserializeSubmission(content: String, format: SubFormat): Submission =
        serializer.deserializeSubmission(content, format)

    override fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission =
        fileListSerializer.deserializeFileList(serializer.deserializeSubmission(content, format), source)

    override fun deserializeSubmission(file: File): Submission =
        deserializeSubmission(readAsPageTab(file).readText(), SubFormat.fromFile(file))

    override fun deserializeSubmission(file: File, source: FilesSource): Submission =
        fileListSerializer.deserializeFileList(deserializeSubmission(file), source)

    override fun serializeFileList(table: FilesTable, format: SubFormat, file: File): File {
        file.outputStream().use { serializer.serializeFileList(table, format, it) }
        return file
    }

    override fun deserializeFileList(fileName: String, source: FilesSource): FileList =
        fileListSerializer.deserializeFileList(fileName, source)

    override fun deserializeFileList(input: InputStream, format: SubFormat): Sequence<PagetabFile> =
        serializer.deserializeFileList(input, format)
}
