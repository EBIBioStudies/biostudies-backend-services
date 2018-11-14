package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.File
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.paths.FolderResolver
import ebi.ac.uk.util.collections.ifNotEmpty
import org.apache.commons.io.FileUtils
import java.nio.file.Files

const val INVALID_FILES_ERROR_MSG = "Submission contains invalid files"

class FilesHandler(private val folderResolver: FolderResolver, private val serializationService: SerializationService) {

    fun processFiles(submission: ExtendedSubmission) {
        generateOutputFiles(submission)
        validateFiles(submission)
        copyFiles(submission)
    }

    private fun generateOutputFiles(submission: ExtendedSubmission) {
        val json = serializationService.serializeSubmission(submission, SubFormat.JSON)
        val xml = serializationService.serializeSubmission(submission, SubFormat.XML)
        val tsv = serializationService.serializeSubmission(submission, SubFormat.TSV)

        val accNo: String = submission.accNo
        val submissionPath = folderResolver.getSubmissionFolder(submission)

        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.tsv").toFile(), tsv, Charsets.UTF_8)
    }

    private fun validateFiles(submission: ExtendedSubmission) {
        val userPath = getUserFolder(submission)

        submission.allFiles()
                .filter { file -> Files.exists(userPath.resolve(file.name)).not() }
                .ifNotEmpty { throw InvalidFilesException(it, INVALID_FILES_ERROR_MSG) }
    }

    private fun copyFiles(submission: ExtendedSubmission) {
        val userPath = getUserFolder(submission)

        submission.allFiles().forEach { file ->
            val sourceFile = userPath.resolve(file.name).toFile()
            val submissionFile = folderResolver.getSubFilePath(submission.relPath, file.name).toFile()
            FileUtils.copyFile(sourceFile, submissionFile)
        }
    }

    private fun getUserFolder(submission: ExtendedSubmission) =
            folderResolver.getUserMagicFolderPath(submission.user.id, submission.user.secretKey)
}

class InvalidFilesException(val invalidFiles: List<File>, message: String) : Exception(message)
