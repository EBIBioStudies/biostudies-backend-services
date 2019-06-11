package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.submission.model.UserSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.File
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.model.extensions.allReferencedFiles
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.apache.commons.io.FileUtils

class FilesCopier(private val folderResolver: SubmissionFolderResolver) {
    fun copy(submission: ExtendedSubmission, filesSource: UserSource) {
        copySubmissionFiles(submission, filesSource)
        copyReferencedFiles(submission, filesSource)
    }

    private fun copySubmissionFiles(submission: ExtendedSubmission, filesSource: UserSource) =
        copy(submission.allFiles(), submission, filesSource)

    private fun copyReferencedFiles(submission: ExtendedSubmission, filesSource: UserSource) =
        copy(submission.allReferencedFiles(), submission, filesSource)

    private fun copy(files: List<File>, submission: ExtendedSubmission, filesSource: UserSource) {
        files.forEach { targetFile ->
            val submissionFile = folderResolver.getSubFilePath(submission.relPath, targetFile.path).toFile()

            // TODO Refactor the size calculation out in order to avoid mixing concerns
            targetFile.size = filesSource.size(targetFile.path)
            FileUtils.copyFile(filesSource.getFile(targetFile.path), submissionFile)
        }
    }
}
