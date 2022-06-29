package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.service.PageTabFileReader.readAsPageTab
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal class PageTabSerializationService(
    private val serializer: PagetabSerializer,
    private val fileListSerializer: FileListSerializer,
) : SerializationService {
    override fun serializeSubmission(submission: Submission, format: SubFormat) =
        serializer.serializeSubmission(submission, format)

    override fun deserializeSubmission(content: String, format: SubFormat): Submission =
        serializer.deserializeSubmission(content, format)

    override fun deserializeSubmission(content: String, format: SubFormat, source: FileSourcesList): Submission =
        fileListSerializer.deserializeFileList(serializer.deserializeSubmission(content, format), source)

    override fun deserializeSubmission(file: File): Submission {
        val pagetabFile = readAsPageTab(file)
        return deserializeSubmission(pagetabFile.readText(), SubFormat.fromFile(pagetabFile))
    }

    override fun deserializeSubmission(file: File, source: FileSourcesList): Submission =
        fileListSerializer.deserializeFileList(deserializeSubmission(file), source)

    override fun deserializeFileList(
        inputStream: InputStream,
        format: SubFormat,
    ): Sequence<BioFile> = fileListSerializer.deserializeFileList(inputStream, format)

    override fun serializeTable(table: FilesTable, format: SubFormat, file: File): File {
        file.outputStream().use { serializer.serializeFileList(table.elements.asSequence(), format, it) }
        return file
    }

    override fun serializeFileList(
        files: Sequence<BioFile>,
        targetFormat: SubFormat,
        outputStream: OutputStream,
    ) {
        serializer.serializeFileList(files, targetFormat, outputStream)
    }
}
