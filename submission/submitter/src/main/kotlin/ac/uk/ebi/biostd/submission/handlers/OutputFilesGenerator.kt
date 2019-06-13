package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.extensions.allLibraryFileSections
import ebi.ac.uk.model.extensions.libraryFileAttr
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.apache.commons.io.FileUtils

class OutputFilesGenerator(
    private val folderResolver: SubmissionFolderResolver,
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
            val libFileName = it.libraryFile!!.name.substringBeforeLast(".")
            val filesTable = FilesTable(it.libraryFile!!.referencedFiles.toList())
            val section = Section()
            section.addFilesTable(filesTable)
            it.libraryFileAttr = libFileName
            it.libraryFile!!.name = libFileName
            generateOutputFiles(section, submission, libFileName)
        }

    private fun <T> generateOutputFiles(element: T, submission: ExtendedSubmission, outputFileName: String) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)
        val submissionPath = folderResolver.getSubmissionFolder(submission)

        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.pagetab.tsv").toFile(), tsv, Charsets.UTF_8)
    }
}
