package ebi.ac.uk.paths

import ebi.ac.uk.extended.model.ExtSubmission
import java.nio.file.Path

const val FILES_PATH = "Files"

// TODO should this be an application property for security reasons?
const val PRIVATE_PATH = ".private"

class SubmissionFolderResolver(
    private val submissionFolder: Path,
    private val ftpFolder: Path
) {
    fun getSubmissionFtpFolder(submissionRelPath: String): Path = ftpFolder.resolve(submissionRelPath)

    fun getSubFolder(submissionRelPath: String): Path = submissionFolder.resolve(submissionRelPath)

    fun getPrivateSubFolderRoot(secret: String): Path {
        return submissionFolder.resolve("$PRIVATE_PATH/${secret.take(2)}")
    }

    fun getPrivateSubFolder(secret: String, relPath: String): Path {
        return getPrivateSubFolderRoot(secret).resolve("${secret.substring(2)}/$relPath")
    }

    fun getSubFilePath(relPath: String, fileName: String): Path =
        submissionFolder.resolve(relPath).resolve(FILES_PATH).resolve(escapeFileName(fileName))

    private fun escapeFileName(fileName: String) = fileName.removePrefix("/")
}
