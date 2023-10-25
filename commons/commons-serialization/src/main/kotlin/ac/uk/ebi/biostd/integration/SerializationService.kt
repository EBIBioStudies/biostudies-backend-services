package ac.uk.ebi.biostd.integration

import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.flow.Flow
import org.jetbrains.annotations.Blocking
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface SerializationService {
    fun serializeSubmission(submission: Submission, format: SubFormat): String

    @Blocking
    fun serializeTable(table: FilesTable, format: SubFormat, file: File): File

    @Blocking
    fun serializeFileList(files: Sequence<BioFile>, targetFormat: SubFormat, outputStream: OutputStream)

    fun deserializeSubmission(content: String, format: SubFormat): Submission

    @Blocking
    fun deserializeSubmission(file: File): Submission

    @Blocking
    fun deserializeFileListAsSequence(inputStream: InputStream, format: SubFormat): Sequence<BioFile>

    suspend fun serializeFileList(files: Flow<BioFile>, targetFormat: SubFormat, outputStream: OutputStream)

    suspend fun deserializeSubmission(content: String, format: SubFormat, source: FileSourcesList): Submission

    suspend fun deserializeSubmission(file: File, source: FileSourcesList): Submission
}
