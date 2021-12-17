package ac.uk.ebi.biostd.integration

import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import java.io.File

interface SerializationService {
    fun deserializeSubmission(content: String, format: SubFormat): Submission

    fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission

    fun deserializeSubmission(file: File): Submission

    fun deserializeSubmission(file: File, source: FilesSource): Submission

    fun deserializeFileList(fileName: String, source: FilesSource): FileList

    fun serializeSubmission(submission: Submission, format: SubFormat): String

    // Allow only submission elements instead of generic i.e. T: SubmissionElement
    fun <T> serializeElement(element: T, format: SubFormat): String

    fun serializeFileList(fileList: FilesTable, format: SubFormat): String
}
