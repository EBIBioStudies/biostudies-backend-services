package ebi.ac.uk.paths

import ebi.ac.uk.extended.model.ExtSubmission
import java.nio.file.Path

const val FILES_PATH = "Files"
const val SUBMISSION_PATH = "submission"
const val FTP_FOLDER = "submission/ftp"

class SubmissionFolderResolver(private val basePath: Path) {

    fun getSubmissionFtpFolder(submission: ExtSubmission): Path =
        basePath.resolve(FTP_FOLDER).resolve(submission.relPath)

    fun getSubmissionFolder(submission: ExtSubmission): Path =
        basePath.resolve(SUBMISSION_PATH).resolve(submission.relPath)

    fun getSubmissionFolder(submissionRelPath: String): Path =
        basePath.resolve(SUBMISSION_PATH).resolve(submissionRelPath)

    fun getSubFilePath(relPath: String, fileName: String): Path =
        basePath.resolve(SUBMISSION_PATH).resolve(relPath).resolve(FILES_PATH).resolve(escapeFileName(fileName))

    private fun escapeFileName(fileName: String) = fileName.removePrefix("/")
}
