package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ac.uk.ebi.biostd.submission.model.FilesSource
import ac.uk.ebi.biostd.submission.model.ListFilesSource
import ac.uk.ebi.biostd.submission.model.PathFilesSource
import ac.uk.ebi.biostd.submission.model.ResourceFile
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.paths.FolderResolver
import ebi.ac.uk.util.collections.ifNotEmpty
import org.apache.commons.io.FileUtils

const val INVALID_FILES_ERROR_MSG = "Submission contains invalid files"

class FilesHandler(private val folderResolver: FolderResolver, private val serializationService: SerializationService) {

    fun processFiles(submission: ExtendedSubmission, files: List<ResourceFile>) {
        val fileSource = if (files.isEmpty()) PathFilesSource(getUserFolder(submission)) else ListFilesSource(files)

        validateFiles(submission, fileSource)
        copyFiles(submission, fileSource)
        generateOutputFiles(submission)
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

    private fun validateFiles(submission: ExtendedSubmission, filesSource: FilesSource) {
        submission.allFiles()
                .filter { file -> filesSource.exists(file.path).not() }
                .ifNotEmpty { throw InvalidFilesException(it, INVALID_FILES_ERROR_MSG) }
    }

    private fun copyFiles(submission: ExtendedSubmission, filesSource: FilesSource) {
        submission.allFiles().forEach { file ->
            val submissionFile = folderResolver.getSubFilePath(submission.relPath, file.path).toFile()
            FileUtils.copyInputStreamToFile(filesSource.getInputStream(file.path), submissionFile)
        }
    }

    private fun getUserFolder(submission: ExtendedSubmission) =
        folderResolver.getUserMagicFolderPath(submission.user.id, submission.user.secretKey)
}
