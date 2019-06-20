package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.extensions.allFileListSections
import ebi.ac.uk.model.extensions.fileListAttr
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.apache.commons.io.FileUtils

class OutputFilesGenerator(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    fun generate(submission: ExtendedSubmission) {
        generateFileList(submission)
        generateSubmissionFiles(submission)
    }

    private fun generateSubmissionFiles(submission: ExtendedSubmission) =
        generateOutputFiles(submission.asSubmission(), submission, submission.accNo)

    private fun generateFileList(submission: ExtendedSubmission) =
        submission.allFileListSections().forEach {
            val fileListName = it.fileList!!.name.substringBeforeLast(".")
            val filesTable = FilesTable(it.fileList!!.referencedFiles.toList())

            it.fileListAttr = fileListName
            it.fileList!!.name = fileListName
            generateOutputFiles(filesTable, submission, fileListName)
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
