package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.mapping.serialization.to.toFilesTable
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileListSections
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.extended.model.allReferencedFiles
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FilePersistenceService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    fun persistSubmissionFiles(submission: ExtSubmission) {
        copyFiles(submission)
        generateOutputFiles(submission)
    }

    private fun generateOutputFiles(submission: ExtSubmission) {
        val simpleSubmission = submission.toSimpleSubmission()

        generateOutputFiles(simpleSubmission, submission.relPath, submission.accNo)
        submission.allFileListSections.forEach {
            generateOutputFiles(it.toFilesTable(), submission.relPath, it.fileName)
        }
    }

    // TODO add file list content validation to integration tests
    private fun <T> generateOutputFiles(element: T, relPath: String, outputFileName: String) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)
        val submissionPath = folderResolver.getSubmissionFolder(relPath)

        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.pagetab.tsv").toFile(), tsv, Charsets.UTF_8)
    }

    private fun moveFiles(submission: ExtSubmission) {
        val submissionFolder = getSubmissionFolder(submission.relPath)
        val temporally = createTempFolder(submissionFolder, submission.accNo)

        submission.allFiles.forEach { it.file.renameTo(temporally.resolve(it.fileName)) }
        submission.allReferencedFiles.forEach { it.file.renameTo(temporally.resolve(it.fileName)) }

        FileUtils.cleanDirectory(submissionFolder)
        temporally.renameTo(submissionFolder.resolve(FILES_PATH))
    }

    // TODO before all process start will not work
    private fun copyFiles(submission: ExtSubmission) {
        val submissionFolder = folderResolver.getSubmissionFolder(submission.relPath).toFile()
        val temporally = createTempFolder(submissionFolder, submission.accNo)

        submission.allFiles.forEach { copy(it, temporally) }
        submission.allReferencedFiles.forEach { copy(it, temporally) }

        FileUtils.cleanDirectory(submissionFolder)
        temporally.renameTo(submissionFolder.resolve(FILES_PATH))
    }

    private fun copy(file: ExtFile, path: File) {
        val targetPath = path.resolve(file.fileName).toPath()
        Files.createDirectories(targetPath)
        Files.copy(file.file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun getSubmissionFolder(relPath: String): File {
        val submissionFolder = folderResolver.getSubmissionFolder(relPath).toFile()
        submissionFolder.mkdirs()
        return submissionFolder
    }

    private fun createTempFolder(submissionFolder: File, accNo: String): File {
        val tempDir = submissionFolder.parentFile.resolve("${accNo}_temp")
        tempDir.mkdirs()
        return tempDir
    }
}
