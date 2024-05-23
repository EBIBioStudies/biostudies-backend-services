package ebi.ac.uk.paths

import java.nio.file.Path

const val FILES_PATH = "Files"

interface SubmissionFolderResolver {
    fun getPublicSubFolder(submissionRelPath: String): Path

    fun getPrivateSubFolder(
        secretKey: String,
        relPath: String,
    ): Path
}
