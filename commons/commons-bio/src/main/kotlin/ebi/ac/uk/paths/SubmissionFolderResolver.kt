package ebi.ac.uk.paths

import ebi.ac.uk.model.ExtendedSubmission
import java.nio.file.Path

private const val FILES_PATH = "Files"
private const val SUBMISSION_PATH = "submission"

class SubmissionFolderResolver(private val basePath: Path) {
    fun getSubmissionFolder(submission: ExtendedSubmission): Path =
        basePath.resolve(SUBMISSION_PATH).resolve(submission.relPath)

    fun getSubFilePath(relPath: String, fileName: String): Path =
        basePath.resolve(SUBMISSION_PATH).resolve(relPath).resolve(FILES_PATH).resolve(escapeFileName(fileName))

    private fun escapeFileName(fileName: String) = fileName.removePrefix("/")
}
