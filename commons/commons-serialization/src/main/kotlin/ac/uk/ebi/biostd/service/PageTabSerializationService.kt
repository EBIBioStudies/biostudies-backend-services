package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.service.PageTabFileReader.readAsPageTab
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal class PageTabSerializationService(
    private val serializer: PagetabSerializer,
    private val fileListSerializer: FileListSerializer,
) : SerializationService {
    override fun serializeSubmission(
        submission: Submission,
        format: SubFormat,
    ): String = serializer.serializeSubmission(submission, format)

    override fun deserializeSubmission(
        content: String,
        format: SubFormat,
    ): Submission = serializer.deserializeSubmission(content, format)

    override suspend fun deserializeSubmission(
        content: String,
        format: SubFormat,
        source: FileSourcesList,
    ): Submission = fileListSerializer.deserializeSubmission(serializer.deserializeSubmission(content, format), source)

    override fun deserializeSubmission(file: File): Submission {
        val pagetabFile = readAsPageTab(file)
        return deserializeSubmission(pagetabFile.readText(), SubFormat.fromFile(pagetabFile))
    }

    override suspend fun deserializeSubmission(
        file: File,
        source: FileSourcesList,
    ): Submission = fileListSerializer.deserializeSubmission(deserializeSubmission(file), source)

    override fun deserializeFileListAsFlow(
        inputStream: InputStream,
        format: SubFormat,
    ): Flow<BioFile> = fileListSerializer.deserializeFileListAsFlow(inputStream, format)

    override fun deserializeLinkListAsFlow(
        inputStream: InputStream,
        format: SubFormat,
    ): Flow<Link> = fileListSerializer.deserializeLinkListAsFlow(inputStream, format)

    override suspend fun serializeTable(
        table: FilesTable,
        format: SubFormat,
        file: File,
    ): File {
        file.outputStream().use { serializer.serializeFileList(table.elements.asFlow(), format, it) }
        return file
    }

    override suspend fun serializeFileList(
        files: Flow<BioFile>,
        targetFormat: SubFormat,
        outputStream: OutputStream,
    ) {
        serializer.serializeFileList(files, targetFormat, outputStream)
    }

    override suspend fun serializeLinkList(
        links: Flow<Link>,
        targetFormat: SubFormat,
        outputStream: OutputStream,
    ) {
        serializer.serializeLinkList(links, targetFormat, outputStream)
    }
}
