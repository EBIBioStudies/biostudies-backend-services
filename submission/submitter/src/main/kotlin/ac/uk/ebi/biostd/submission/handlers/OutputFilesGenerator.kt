package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.extensions.allLibraryFileSections
import ebi.ac.uk.model.extensions.libraryFileAttr
import ebi.ac.uk.paths.FolderResolver
import org.apache.commons.io.FileUtils

class OutputFilesGenerator(
    private val folderResolver: FolderResolver,
    private val serializationService: SerializationService
) {
    fun generate(submission: ExtendedSubmission) {
        generateLibraryFiles(submission)
        generateSubmissionFiles(submission)
    }

    private fun generateSubmissionFiles(submission: ExtendedSubmission) =
        generateOutputFiles(submission.asSubmission(), submission, submission.accNo)

    private fun generateLibraryFiles(submission: ExtendedSubmission) =
        submission.allLibraryFileSections().forEach {
            val libFileName = "${submission.accNo}.${it.accNo}.files"
            val filesTable = FilesTable(it.libraryFile!!.referencedFiles.toList())

            it.libraryFileAttr = libFileName
            it.libraryFile!!.name = libFileName
            generateOutputFiles(filesTable, submission, libFileName)
        }

    private fun <T> generateOutputFiles(element: T, submission: ExtendedSubmission, outputFileName: String) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)
        val submissionPath = folderResolver.getSubmissionFolder(submission)

        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.tsv").toFile(), tsv, Charsets.UTF_8)
    }
}
