package ebi.ac.uk.paths

import ebi.ac.uk.model.ExtendedSubmission
import java.nio.file.Path

private const val FILES_PATH = "Files"
private const val SUBMISSION_PATH = "submission"

class FolderResolver(private val basePath: Path) {

    fun getSubmissionFolder(submission: ExtendedSubmission): Path =
        basePath.resolve(SUBMISSION_PATH).resolve(submission.relPath)

    fun getSubFilePath(relPath: String, fileName: String) =
        basePath.resolve(SUBMISSION_PATH).resolve(relPath).resolve(FILES_PATH).resolve(fileName)!!
}
