package ac.uk.ebi.biostd.integration

import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface SerializationService {
    fun serializeSubmission(submission: Submission, format: SubFormat): String

    fun serializeFileList(table: FilesTable, format: SubFormat, file: File): File

    fun serializeFileList(files: Sequence<ebi.ac.uk.model.File>, targetFormat: SubFormat, outputStream: OutputStream)

    fun deserializeSubmission(content: String, format: SubFormat): Submission

    fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission

    fun deserializeSubmission(file: File): Submission

    fun deserializeSubmission(file: File, source: FilesSource): Submission

    fun deserializeFileList(inputStream: InputStream): Sequence<ebi.ac.uk.model.File>
}
