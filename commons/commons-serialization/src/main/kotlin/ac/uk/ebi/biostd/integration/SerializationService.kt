package ac.uk.ebi.biostd.integration

import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface SerializationService {
    fun serializeSubmission(submission: Submission, format: SubFormat): String

    fun serializeTable(table: FilesTable, format: SubFormat, file: File): File

    fun serializeFileList(files: Sequence<BioFile>, targetFormat: SubFormat, outputStream: OutputStream)

    suspend fun serializeFileList(files: Flow<BioFile>, targetFormat: SubFormat, outputStream: OutputStream)

    fun deserializeSubmission(content: String, format: SubFormat): Submission

    suspend fun deserializeSubmission(content: String, format: SubFormat, source: FileSourcesList): Submission

    fun deserializeSubmission(file: File): Submission

    suspend fun deserializeSubmission(file: File, source: FileSourcesList): Submission

    fun deserializeFileList(inputStream: InputStream, format: SubFormat): Sequence<BioFile>
}
