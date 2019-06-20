package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.service.SubFileResolver
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.extended.model.allLibraryFiles
import ebi.ac.uk.extended.model.allReferencedFiles
import ebi.ac.uk.model.User
import org.apache.commons.io.FileUtils
import java.nio.file.Path

class Submitter(
    private val folderResolver: SubFileResolver,
    private val serializationService: SerializationService) {

    fun submitSubmission(submission: ExtSubmission, user: User) {
        val submissionPath = folderResolver.getSubmissionFolder(submission.relPath)
        submission.allFiles.forEach { file -> copyFile(submissionPath, file) }
        submission.allReferencedFiles.forEach { file -> copyFile(submissionPath, file) }

        generateOutputFiles(submission.toSimpleSubmission(), submissionPath, submission.accNo)
        submission.allLibraryFiles.forEach { generateOutputFiles(it.referencedFiles, submissionPath, it.fileName) }
    }

    private fun <T> generateOutputFiles(element: T, submissionPath: Path, outputFileName: String) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)

        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.pagetab.tsv").toFile(), tsv, Charsets.UTF_8)
    }

    //TODO create folder if not exists
    private fun copyFile(submissionPath: Path, file: ExtFile) {
        val submissionFile = submissionPath.resolve(file.fileName).toFile()
        FileUtils.copyFile(file.file, submissionFile)
    }
}
