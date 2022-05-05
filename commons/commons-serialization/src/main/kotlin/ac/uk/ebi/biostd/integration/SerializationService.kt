package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.service.FileListSerializer
import ac.uk.ebi.biostd.service.PageTabSerializationService
import ac.uk.ebi.biostd.service.PagetabSerializer
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface SerializationService {
    fun serializeSubmission(submission: Submission, format: SubFormat): String

    fun serializeTable(table: FilesTable, format: SubFormat, file: File): File

    fun serializeFileList(files: Sequence<BioFile>, targetFormat: SubFormat, outputStream: OutputStream)

    fun deserializeSubmission(content: String, format: SubFormat): Submission

    fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission

    fun deserializeSubmission(file: File): Submission

    fun deserializeSubmission(file: File, source: FilesSource): Submission

    fun deserializeFileList(inputStream: InputStream, format: SubFormat): Sequence<BioFile>

    companion object {
        operator fun invoke(): SerializationService = instance

        private val instance = create()
        private fun create(): SerializationService {
            return PageTabSerializationService(
                PagetabSerializer(),
                FileListSerializer(PagetabSerializer())
            )
        }
    }
}
