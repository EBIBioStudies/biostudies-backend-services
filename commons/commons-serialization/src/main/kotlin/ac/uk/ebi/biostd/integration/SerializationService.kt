package ac.uk.ebi.biostd.integration

import kotlinx.coroutines.flow.Flow
import uk.ac.ebi.biostd.io.sources.FileSourcesList
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.FilesTable
import uk.ac.ebi.biostd.model.Link
import uk.ac.ebi.biostd.model.Submission
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface SerializationService {
    fun serializeSubmission(
        submission: Submission,
        format: SubFormat,
    ): String

    suspend fun serializeTable(
        table: FilesTable,
        format: SubFormat,
        file: File,
    ): File

    fun deserializeSubmission(
        content: String,
        format: SubFormat,
    ): Submission

    suspend fun deserializeSubmission(file: File): Submission

    fun deserializeFileListAsFlow(
        inputStream: InputStream,
        format: SubFormat,
    ): Flow<BioFile>

    fun deserializeLinkListAsFlow(
        inputStream: InputStream,
        format: SubFormat,
    ): Flow<Link>

    suspend fun serializeFileList(
        files: Flow<BioFile>,
        targetFormat: SubFormat,
        outputStream: OutputStream,
    )

    suspend fun serializeLinkList(
        links: Flow<Link>,
        targetFormat: SubFormat,
        outputStream: OutputStream,
    )

    suspend fun deserializeSubmission(
        content: String,
        format: SubFormat,
        source: FileSourcesList,
    ): Submission

    suspend fun deserializeSubmission(
        file: File,
        source: FileSourcesList,
    ): Submission
}
