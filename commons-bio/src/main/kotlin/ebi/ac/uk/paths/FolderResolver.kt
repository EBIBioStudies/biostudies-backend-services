package ebi.ac.uk.paths

import ebi.ac.uk.model.ExtendedSubmission
import java.nio.file.Path
import java.nio.file.Paths

internal const val USER_FOLDER_PREFIX = "a"
internal const val GROUP_FOLDER_PREFIX = "b"

private const val FILES_PATH = "Files"
private const val SUBMISSION_PATH = "submission"

class FolderResolver(private val basePath: Path) {

    fun getSubmissionFolder(submission: ExtendedSubmission): Path =
        basePath.resolve(SUBMISSION_PATH).resolve(submission.relPath)

    fun getSubFilePath(relPath: String, fileName: String) =
        basePath.resolve(relPath).resolve(FILES_PATH).resolve(fileName)!!

    fun getUserMagicFolderPath(userId: Long, secret: String) = getMagicFolderPath(userId, secret, USER_FOLDER_PREFIX)

    fun getGroupMagicFolderPath(groupId: Long, secret: String) = getMagicFolderPath(groupId, secret, GROUP_FOLDER_PREFIX)

    private fun getMagicFolderPath(id: Long, secret: String, separator: String): Path {
        val parent = "$basePath/${secret.substring(0, 2)}"
        return Paths.get("$parent/${secret.substring(2)}-$separator$id")
    }
}