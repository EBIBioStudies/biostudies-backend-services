package ebi.ac.uk.paths

import ebi.ac.uk.model.ExtendedSubmission
import java.nio.file.Path
import java.nio.file.Paths

internal const val USER_FOLDER_PREFIX = "a"
internal const val GROUP_FOLDER_PREFIX = "b"

private const val FILES_PATH = "Files"
private const val SUBMISSION_PATH = "submission"

// TODO: split by submission file resolver in submitter module and userFileResolver in security module
class FolderResolver(private val basePath: Path, private val filesDirPath: Path) {

    fun getSubmissionFolder(submission: ExtendedSubmission): Path =
        basePath.resolve(SUBMISSION_PATH).resolve(submission.relPath)

    fun getSubFilePath(relPath: String, fileName: String) =
        basePath.resolve(SUBMISSION_PATH).resolve(relPath).resolve(FILES_PATH).resolve(fileName)!!

    fun getUserMagicFolderPath(userId: Long, secret: String) = getMagicFolderPath(userId, secret, USER_FOLDER_PREFIX)

    fun getGroupMagicFolderPath(groupId: Long, secret: String) =
        getMagicFolderPath(groupId, secret, GROUP_FOLDER_PREFIX)

    /**
     * Gets the magic folder path for user and groups. Magic folder path is as follows:
     *
     * The parent folder is defined by the two first letters of the secret.
     * The secret folder is defined by the characters of the secret from position 3 plus the entity type letter ('a' for
     * users, 'b' for groups) and finally the entity id.
     *
     * So for example, for a user with secret abc-123 and id=50, the secret path will be /ab/c-123-a50
     */
    private fun getMagicFolderPath(id: Long, secret: String, separator: String): Path {
        return Paths.get("$filesDirPath/${secret.substring(0, 2)}/${secret.substring(2)}-$separator$id")
    }
}
