package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.biostd.submission.model.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.extensions.libFileSections
import ebi.ac.uk.paths.FolderResolver
import org.apache.commons.io.FileUtils

class OutputFilesGenerator(
    private val folderResolver: FolderResolver,
    private val serializationService: SerializationService
) {
    fun generate(submission: ExtendedSubmission, filesSource: FilesSource) {
        generateSubmissionFiles(submission)
        generateLibraryFiles(submission, filesSource)
    }

    private fun generateSubmissionFiles(submission: ExtendedSubmission) =
        generateOutputFiles(submission.asSubmission(), submission, submission.accNo)

    private fun generateLibraryFiles(submission: ExtendedSubmission, filesSource: FilesSource) =
        submission.libFileSections().forEach {
            val libFileName = "${submission.accNo}.${it.accNo}.files"
            val libFileContent = filesSource.readText(it.libraryFile!!.name)
            val filesTable = serializationService.deserializeElement<FilesTable>(libFileContent, SubFormat.TSV)

            it.libraryFile!!.name = "$libFileName.tsv"
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
