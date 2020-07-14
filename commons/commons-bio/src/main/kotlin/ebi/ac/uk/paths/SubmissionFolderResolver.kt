package ebi.ac.uk.paths

import java.nio.file.Path

const val FILES_PATH = "Files"

class SubmissionFolderResolver(
    private val submissionFolder: Path,
    private val ftpFolder: Path
) {

    fun getSubmissionFtpFolder(submissionRelPath: String): Path =
        ftpFolder.resolve(submissionRelPath)

    fun getSubmissionFolder(submissionRelPath: String): Path =
        submissionFolder.resolve(submissionRelPath)

    fun getSubFilePath(relPath: String, fileName: String): Path =
        submissionFolder.resolve(relPath).resolve(FILES_PATH).resolve(escapeFileName(fileName))

    private fun escapeFileName(fileName: String) = fileName.removePrefix("/")
}
