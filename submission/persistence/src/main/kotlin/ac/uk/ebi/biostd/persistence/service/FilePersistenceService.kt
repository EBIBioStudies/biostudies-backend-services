package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileListSections
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.extended.model.allReferencedFiles
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.apache.commons.io.FileUtils
import java.io.File

class FilePersistenceService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    fun persistSubmissionFiles(submission: ExtSubmission) {
        persistFiles(submission)
        generateOutputFiles(submission)
    }

    private fun persistFiles(submission: ExtSubmission) {
        copy(submission.allFiles, submission.relPath)
        copy(submission.allReferencedFiles, submission.relPath)
    }

    private fun generateOutputFiles(submission: ExtSubmission) {
        generateOutputFiles(submission.toSimpleSubmission(), submission, submission.accNo)
        submission.allFileListSections.forEach { generateOutputFiles(it, submission, it.fileName) }
    }

    private fun copy(files: List<ExtFile>, submissionRelPath: String) {
        files.forEach { extFile ->
            val submissionFile = folderResolver.getSubFilePath(submissionRelPath, extFile.fileName).toFile()
            FileUtils.copyFile(extFile.file, submissionFile)
        }
    }

    private fun smartCopy(source: File, target: File) {
        
    }

    fun <T> generateOutputFiles(element: T, submission: ExtSubmission, outputFileName: String) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)
        val submissionPath = folderResolver.getSubmissionFolder(submission.relPath)

        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.pagetab.tsv").toFile(), tsv, Charsets.UTF_8)
    }
}
